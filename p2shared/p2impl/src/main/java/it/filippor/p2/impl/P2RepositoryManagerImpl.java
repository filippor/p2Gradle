package it.filippor.p2.impl;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
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

import it.filippor.p2.api.DefaultRepo;
import it.filippor.p2.api.P2RepositoryManager;

@Component()
public class P2RepositoryManagerImpl implements P2RepositoryManager {

  private static final String PROFILE = "my_install1";
  BundleContext               ctx;

  @Activate
  public void activate(BundleContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Object resolve(DefaultRepo repo, String site, String artifactId, String version) {
    IProvisioningAgent agent = null;
    try {
      IProgressMonitor externalMonitor = new IProgressMonitor() {
 int worked = 0;
                                         @Override
                                         public void worked(int work) {
                                           System.out.println("worked " +( worked += work));
                                         }

                                         @Override
                                         public void subTask(String name) {
//                                           System.out.println("sub " + name);
                                         }

                                         @Override
                                         public void setTaskName(String name) {
                                           System.out.println("task " + name);
                                         }

                                         @Override
                                         public void setCanceled(boolean value) {
                                         }

                                         @Override
                                         public boolean isCanceled() {
                                           return false;
                                         }

                                         @Override
                                         public void internalWorked(double work) {
                                           System.out.println("internal worked " + work);

                                         }

                                         @Override
                                         public void done() {
                                           System.out.println("done ");
                                         }

                                         @Override
                                         public void beginTask(String name, int totalWork) {
                                           System.out.println("beginTask " + name + " " + totalWork);

                                         }
                                       };
      SubMonitor       mon             = SubMonitor.convert(externalMonitor, "resolve", 1000);

      agent = getAgent(repo);
      Object result;

      // get the repository managers
      IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) agent
        .getService(IArtifactRepositoryManager.SERVICE_NAME);

      artifactManager.addRepository(URI.create(site));
      artifactManager.loadRepository(URI.create(site), mon.split(250));
      // Load and query the metadata
      IMetadataRepositoryManager manager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
      manager.addRepository(URI.create(site));
      IMetadataRepository metadataRepo = manager.loadRepository(URI.create(site), mon.split(250));

      Set<IInstallableUnit> toInstall = metadataRepo.query(QueryUtil.createIUQuery(artifactId), mon.split(100))
        .toUnmodifiableSet();

      Set<File> files = getOrInstallFile(agent, mon.split(400), artifactManager, toInstall);
      result = files;
      mon.done();
      return result;
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
                              Set<IInstallableUnit> toInstall) throws ProvisionException, InterruptedException,
                                                               ExecutionException {
    monitor = SubMonitor.convert(monitor,1000);
    var localRepos = getLocalFileRepo(artifactManager, monitor.split(100));

    Set<File>             files     = new HashSet<>();
    
    int weight4iu = 800/toInstall.size();
    Set<IInstallableUnit> missingIU = tryTogetFromRepo(toInstall, localRepos, files, monitor,weight4iu);
    monitor.worked(100);
    if (!missingIU.isEmpty()) {
      files.addAll(install(agent, missingIU, monitor.split(weight4iu * missingIU.size())));

    }
    monitor.done();
    return files;
  }

  private Set<IInstallableUnit> tryTogetFromRepo(Set<IInstallableUnit> toInstall, List<IFileArtifactRepository> localRepos,
                                                 Set<File> files, SubMonitor mon,int weight4IU) {
   
    Set<IInstallableUnit> missingIU = new HashSet<>();
    for (IInstallableUnit iu : toInstall) {
      for (IArtifactKey a : iu.getArtifacts()) {
        Set<File> foundInRepo = findInRepo(localRepos, a);
        if (foundInRepo.isEmpty()) {
          missingIU.add(iu);
          System.out.println("missing " + iu);
        } else {
          System.out.println("found " + foundInRepo);
          files.addAll(foundInRepo);
          mon.worked(weight4IU);
        }
      }
    }
    mon.done();
    return missingIU;
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

  private List<IFileArtifactRepository> getLocalFileRepo(IArtifactRepositoryManager artifactManager, SubMonitor monitor) {
    URI[] knownRepositories = artifactManager.getKnownRepositories(IRepositoryManager.REPOSITORIES_LOCAL);

    var localRepos = Arrays.asList(knownRepositories).stream().map(uri -> {
      try {
        IArtifactRepository loadRepository;
        loadRepository = artifactManager.loadRepository(uri, monitor.split(1));
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

  private Set<File> install(IProvisioningAgent agent, Set<IInstallableUnit> toInstall,
                                               IProgressMonitor mon) throws ProvisionException {

    SubMonitor monitor = SubMonitor.convert(mon, "install", 1000);
    // Creating an operation
    InstallOperation installOperation = new InstallOperation(new ProvisioningSession(agent), toInstall);
    IProfileRegistry profileRegistry  = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
    if (profileRegistry.getProfile(PROFILE) == null)
      profileRegistry.addProfile(PROFILE);
    installOperation.setProfileId(PROFILE);

    Set<File>                    files  = new HashSet<File>();
    if (installOperation.resolveModal(monitor.split(1)).isOK()) {
      try {
        ProvisioningJob job = installOperation.getProvisioningJob(monitor.split(1));
       
        job.runModal(monitor.split(700));
        // job.schedule();
        job.join();
        IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) agent
          .getService(IArtifactRepositoryManager.SERVICE_NAME);
        tryTogetFromRepo(toInstall, getLocalFileRepo(artifactManager, monitor.split(1)), files, monitor.split(500),300/toInstall.size());
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
}
