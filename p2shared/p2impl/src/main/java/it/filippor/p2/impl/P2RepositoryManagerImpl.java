package it.filippor.p2.impl;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.expression.ExpressionUtil;
import org.eclipse.equinox.p2.metadata.expression.IExpression;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IFileArtifactRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.VersionRange;
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
  public ResolveResult resolve(DefaultRepo repo, Iterable<URI> sites, Iterable<Artifact> artifacts, boolean transitive,
                               ProgressMonitor monitor) {

    IProgressMonitor wrappedMonitor = WrappedMonitor.wrap(monitor);

    IProvisioningAgent agent = null;
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
        manager.loadRepository(site, mon.split(200));

        artifactManager.addRepository(site);
        artifactManager.loadRepository(site, mon.split(200));
      }

      List<IQuery<IInstallableUnit>> queries = StreamSupport.stream(artifacts.spliterator(), false).map(artifact -> {
        return QueryUtil.createIUQuery(artifact.getId(), toVersion(artifact.getVersion()));
      }).collect(Collectors.toList());

      IQuery<IInstallableUnit> iuQuery = QueryUtil.createCompoundQuery(queries, false);

      Set<IInstallableUnit> toInstall = manager.query(iuQuery, mon.split(100)).toSet();
      if (transitive) {
        IExpression matchesRequirementsExpression = ExpressionUtil.parse("$0.exists(r | this ~= r)");

        List<IQuery<IInstallableUnit>> transitiveQuery = toInstall.stream().map(parent -> {
          return QueryUtil.createMatchQuery(matchesRequirementsExpression, parent.getRequirements());
        }).collect(Collectors.toList());
        toInstall.addAll(manager.query(QueryUtil.createCompoundQuery(transitiveQuery, false), mon.split(100)).toSet());
      }

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

  private org.eclipse.equinox.p2.metadata.VersionRange toVersion(VersionRange version) {

    return org.eclipse.equinox.p2.metadata.VersionRange.create(version.toString());
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
    Set<File>             files     = tryTogetFromRepo(toInstall, localRepos, missingIU, monitor.split(600));
    monitor.worked(200);
    int workTodo = (1000 - 200) / toInstall.size() * toInstall.size();
    monitor.setWorkRemaining(workTodo);
    if (!missingIU.isEmpty()) {
      files.addAll(install(agent, missingIU, notFoundIU, monitor.split(workTodo)));
    }
    monitor.done();
    return files;
  }

  private Set<File> tryTogetFromRepo(Set<IInstallableUnit> toInstall, List<IFileArtifactRepository> localRepos,
                                     Set<IInstallableUnit> missingIU, SubMonitor monitor) {
    monitor.beginTask("getFromRepo", toInstall.size() * 2);
    Set<File> files = new HashSet<>();
    for (IInstallableUnit iu : toInstall) {
      for (IArtifactKey a : iu.getArtifacts()) {
        Set<File> foundInRepo = findInRepo(localRepos, a);
        monitor.worked(1);
        monitor.checkCanceled();
        if (foundInRepo.isEmpty()) {
          missingIU.add(iu);
        } else {
          files.addAll(foundInRepo);
          monitor.worked(1);
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
    URI[]      knownRepositories = artifactManager.getKnownRepositories(IRepositoryManager.REPOSITORIES_LOCAL);
    SubMonitor monitor           = SubMonitor.convert(parentMonitor, "get local repo", 100 * knownRepositories.length);

    var localRepos = Arrays.asList(knownRepositories).stream().map(uri -> {
      try {
        IArtifactRepository loadRepository;
        loadRepository = artifactManager.loadRepository(uri, monitor.split(100));
        if (loadRepository instanceof IFileArtifactRepository) {
          return (IFileArtifactRepository) loadRepository;
        }
      } catch (ProvisionException e) {
        sneakyThrow(e);
      }
      return null;
    }).filter(r -> r != null).collect(Collectors.toList());
    return localRepos;
  }

  private Set<File> install(IProvisioningAgent agent, Set<IInstallableUnit> toInstall, Set<IInstallableUnit> missingIU,
                            IProgressMonitor mon) throws ProvisionException {

    SubMonitor monitor = SubMonitor.convert(mon, "install", 1000);
    // Creating an operation
    InstallOperation installOperation = new InstallOperation(new ProvisioningSession(agent), toInstall);
    IProfileRegistry profileRegistry  = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
    if (profileRegistry.getProfile(PROFILE) == null) {
      Map<String, String> props = new HashMap<>();
      props.put("org.eclipse.equinox.p2.roaming", "true");
      profileRegistry.addProfile(PROFILE, props);

    }
    installOperation.setProfileId(PROFILE);

    Set<File> files = new HashSet<File>();
    monitor.checkCanceled();
    if (installOperation.resolveModal(monitor.split(100)).isOK()) {
      try {
        ProvisioningJob job = installOperation.getProvisioningJob(monitor.split(100));

        job.runModal(monitor.split(600));
        job.join();
        IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) agent
          .getService(IArtifactRepositoryManager.SERVICE_NAME);
        monitor.checkCanceled();
        files.addAll(tryTogetFromRepo(toInstall, getLocalFileRepo(artifactManager, monitor.split(1)), missingIU,
                                      monitor.split(200)));
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
    return new Artifact(iu.getId(), new VersionRange(iu.getVersion().getOriginal()));
  }

}
