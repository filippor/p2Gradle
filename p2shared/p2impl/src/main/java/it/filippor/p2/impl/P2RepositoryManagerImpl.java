package it.filippor.p2.impl;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.internal.p2.metadata.OSGiVersion;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.Version;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IFileArtifactRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import it.filippor.p2.api.Artifact;
import it.filippor.p2.api.DefaultRepo;
import it.filippor.p2.api.P2RepositoryManager;
import it.filippor.p2.api.ProgressMonitor;
import it.filippor.p2.api.ResolveResult;

@Component()
public class P2RepositoryManagerImpl implements P2RepositoryManager {

  private static final String PROFILE = "my_install1";
  BundleContext               ctx;

  @Activate
  public void activate(BundleContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public ResolveResult resolve(DefaultRepo repo, Iterable<URI> sites, Iterable<Artifact> artifacts, ProgressMonitor monitor) {
    IProgressMonitor   wrappedMonitor = WrappedMonitor.wrap(monitor);
    IProvisioningAgent agent          = null;
    try {
      SubMonitor mon = SubMonitor.convert(wrappedMonitor, "resolve", 1000);

      agent = getAgent(repo);

      // get the repository managers
      IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) agent
        .getService(IArtifactRepositoryManager.SERVICE_NAME);
      IMetadataRepositoryManager manager         = (IMetadataRepositoryManager) agent
        .getService(IMetadataRepositoryManager.SERVICE_NAME);
    
      for (URI site : sites) {
        manager.addRepository(site);
        manager.loadRepository(site, mon.split(250));

        artifactManager.addRepository(site);
        artifactManager.loadRepository(site, mon.split(250));
      }

      List<IQuery<IInstallableUnit>> queries = StreamSupport.stream(artifacts.spliterator(), false).map(artifact -> {
        return QueryUtil.createIUQuery(artifact.getId()
                                                         /*, toOsgiVersion(artifact.getVersion())*/
                                                        );
      }).collect(Collectors.toList());

      IQuery<IInstallableUnit> iuQuery = QueryUtil.createCompoundQuery(queries, false);

      Set<IInstallableUnit> toInstall = manager.query(iuQuery, mon.split(100)).toUnmodifiableSet();
     
      Set<IInstallableUnit> notFoundIU = new HashSet<>();
      Set<File>             files      = getOrInstallFile(agent, mon.split(400), artifactManager, toInstall, notFoundIU);

      mon.done();

      return new ResolveResult(files, toArtifact(notFoundIU));
    } catch (Exception e) {
      e.printStackTrace();
      sneakyThrow(e);
      return null;
    } finally {
      if (agent != null)
        agent.stop();
    }
  }

  private Set<File> getOrInstallFile(IProvisioningAgent agent, SubMonitor monitor, IArtifactRepositoryManager artifactManager,
                                     Set<IInstallableUnit> toInstall, Set<IInstallableUnit> notFoundIU) throws ProvisionException,
                                                                                                        InterruptedException,
                                                                                                        ExecutionException {
    monitor = SubMonitor.convert(monitor, 1000);
    if (toInstall.isEmpty())
      return new HashSet<>();
    var localRepos = getLocalFileRepo(artifactManager, monitor.split(100));

    Set<IInstallableUnit> missingIU = new HashSet<>();
    int                   weight4iu = 800 / toInstall.size();
    Set<File>             files     = tryTogetFromRepo(toInstall, localRepos, missingIU, monitor, weight4iu);
    monitor.worked(100);
    if (!missingIU.isEmpty()) {
      files.addAll(install(agent, missingIU, notFoundIU, monitor.split(weight4iu * missingIU.size())));
    }
    monitor.done();
    return files;
  }

  private Set<File> tryTogetFromRepo(Set<IInstallableUnit> toInstall, List<IFileArtifactRepository> localRepos,
                                     Set<IInstallableUnit> missingIU, SubMonitor monitor, int weight4IU) {

    Set<File> files = new HashSet<>();
    for (IInstallableUnit iu : toInstall) {
      for (IArtifactKey a : iu.getArtifacts()) {
        Set<File> foundInRepo = findInRepo(localRepos, a);
        if (monitor.isCanceled())
          return null;
        if (foundInRepo.isEmpty()) {
          missingIU.add(iu);
        } else {
          files.addAll(foundInRepo);
          monitor.worked(weight4IU);
        }
      }
    }
    monitor.done();
    return files;
  }

  private Set<File> findInRepo(List<IFileArtifactRepository> localRepos, IArtifactKey a) {
    HashSet<File> files = new HashSet<File>();
    for (IFileArtifactRepository fileRepo : localRepos) {
      IArtifactDescriptor[] artifactDescriptors = fileRepo.getArtifactDescriptors(a);
      for (IArtifactDescriptor descriptor : artifactDescriptors) {
        files.add(fileRepo.getArtifactFile(descriptor));
      }
    }
    return files;
  }

  private List<IFileArtifactRepository> getLocalFileRepo(IArtifactRepositoryManager artifactManager, SubMonitor parentMonitor) {
    SubMonitor monitor           = SubMonitor.convert(parentMonitor, "get local repo", 100);
    URI[]      knownRepositories = artifactManager.getKnownRepositories(IRepositoryManager.REPOSITORIES_LOCAL);
    int        work4repo         = 100 / knownRepositories.length;
    var        localRepos        = Arrays.asList(knownRepositories).stream().map(uri -> {
                                   try {
                                     IArtifactRepository loadRepository;
                                     loadRepository = artifactManager.loadRepository(uri, monitor.split(work4repo));
                                     if (loadRepository instanceof IFileArtifactRepository) {
                                       return (IFileArtifactRepository) loadRepository;
                                     }
                                   } catch (ProvisionException e) {
                                     sneakyThrow(e);
                                   }
                                   return null;
                                 }).filter(r -> {
                                   return r != null;
                                 }).collect(Collectors.toList());
    return localRepos;
  }

  private Set<File> install(IProvisioningAgent agent, Set<IInstallableUnit> toInstall, Set<IInstallableUnit> missingIU,
                            IProgressMonitor mon) throws ProvisionException {

    SubMonitor monitor = SubMonitor.convert(mon, "install", 1000);
    // Creating an operation
    InstallOperation installOperation = new InstallOperation(new ProvisioningSession(agent), toInstall);
    IProfileRegistry profileRegistry  = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
    if (profileRegistry.getProfile(PROFILE) == null)
      profileRegistry.addProfile(PROFILE);
    installOperation.setProfileId(PROFILE);

    Set<File> files = new HashSet<File>();
    if (monitor.isCanceled())
      return null;
    if (installOperation.resolveModal(monitor.split(50)).isOK()) {
      try {
        ProvisioningJob job = installOperation.getProvisioningJob(monitor.split(50));

        job.runModal(monitor.split(600));
        job.join();
        IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) agent
          .getService(IArtifactRepositoryManager.SERVICE_NAME);

        files.addAll(tryTogetFromRepo(toInstall, getLocalFileRepo(artifactManager, monitor.split(1)), missingIU, monitor,
                                      300 / toInstall.size()));
      } catch (InterruptedException e) {
        sneakyThrow(e);
      }
    }
    monitor.done();
    return files;
  }

  private IProvisioningAgent getAgent(DefaultRepo repo) throws ProvisionException {
    ServiceReference<?> sr = ctx.getServiceReference(IProvisioningAgentProvider.SERVICE_NAME);
    if (sr == null)
      throw new IllegalStateException("cannot find agent provider");
    IProvisioningAgentProvider agentProvider = (IProvisioningAgentProvider) ctx.getService(sr);
    IProvisioningAgent         agent         = agentProvider.createAgent(repo.getAgentURI());
    return agent;
  }

  @SuppressWarnings("unchecked")
  public static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
    throw (E) e;
  }

  private Iterable<Artifact> toArtifact(Set<IInstallableUnit> notFoundIU) {
    return notFoundIU.stream().map(this::toArtifact).collect(Collectors.toSet());
  }

  private Artifact toArtifact(IInstallableUnit iu) {
    return new Artifact(iu.getId(), toVersion(iu.getVersion()));
  }

  private it.filippor.p2.api.Version toVersion(Version v) {
    Comparable<?>[] seg = new Comparable<?>[] { null, null, null };
    for (int i = 0; i < v.getSegmentCount() && i < seg.length; i++) {
      seg[i] = v.getSegment(i);
    }
    return new it.filippor.p2.api.Version(toInt(seg[0]), toInt(seg[1]), toInt(seg[2]), seg[3]);
  }

  private int toInt(Comparable<?> seg) {
    if (seg == null)
      return 0;
    return Integer.valueOf((String) seg);
  }

  private static Version toOsgiVersion(it.filippor.p2.api.Version v) {
    return new OSGiVersion(v.major, v.minor, v.minor, v.qualifier);
  }
}
