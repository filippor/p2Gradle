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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactDescriptor;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IFileArtifactRepository;

import it.filippor.p2.impl.util.Result;
import it.filippor.p2.impl.util.Utils;

public class ArtifactRepositoryFacade {
//TODO: get from project
  private static final String           PROFILE = "my_install";
  IProvisioningAgent                    agent;
  private IArtifactRepositoryManager    artifactManager;
  private List<IFileArtifactRepository> localFileRepo;

  public ArtifactRepositoryFacade(IProvisioningAgent agent, Iterable<URI> sites, SubMonitor mon) {
    mon             = SubMonitor.convert(mon, "createArtifactFacade", 20);
    this.agent      = agent;
    artifactManager = (IArtifactRepositoryManager) agent.getService(IArtifactRepositoryManager.SERVICE_NAME);
    setUpdateSite(sites, mon.split(10));
    this.localFileRepo = getLocalFileRepoInternal(mon.split(10));
  }

  private void setUpdateSite(Iterable<URI> sites, SubMonitor mon) {
    for (URI site : sites) {
      artifactManager.addRepository(site);
    }
  }
  
  
  
  public Set<File> getFiles( Set<IInstallableUnit> toInstall,
                                     SubMonitor monitor) throws ProvisionException, InterruptedException, ExecutionException {
    int totalWork = 100 +(toInstall.size() * 200);
    monitor = SubMonitor.convert(monitor, totalWork);
    if (toInstall.isEmpty())
      return new HashSet<>();

    Result<Set<File>, Set<IInstallableUnit>> result = getFromLocalRepo(toInstall, monitor.split(totalWork));
    if (!result.getMiss().isEmpty()) {
      monitor.setWorkRemaining(result.getMiss().size() * 200);
      install(result.getMiss(), monitor.split(result.getMiss().size() * 150));
      reloadLocalRepo(monitor.split(100));
      Result<Set<File>, Set<IInstallableUnit>> result2 = getFromLocalRepo(result.getMiss(),
                                                                                  monitor.split(result.getMiss().size() * 50));
      result2.getHit().addAll(result.getHit());
      result = result2;
    }
    if (!result.getMiss().isEmpty()) {

      throw new RuntimeException("can not find artifacts: "
                                 + result.getMiss().stream().map(m -> m.toString()).collect(Collectors.joining(",")));
    }
    monitor.done();
    return result.getHit();
  }
  
  private List<IFileArtifactRepository> getLocalFileRepoInternal(IProgressMonitor parentMonitor) {
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
        Utils.sneakyThrow(e);
      }
      return null;
    }).filter(r -> r != null).collect(Collectors.toList());
    return localRepos;
  }

  public Result<Set<File>, Set<IInstallableUnit>> getFromLocalRepo(Set<IInstallableUnit> toInstall, SubMonitor monitor) {
    monitor = SubMonitor.convert(monitor,"get from l;ocal repo",toInstall.size()*10);
    Set<File>             files     = new HashSet<>();
    Set<IInstallableUnit> missingIU = new HashSet<>();

    for (IInstallableUnit iu : toInstall) {
      for (IArtifactKey a : iu.getArtifacts()) {
        Set<File> foundInRepo = findInRepo(a);
        monitor.worked(5);
        if (foundInRepo.isEmpty()) {
          missingIU.add(iu);
        } else {
          files.addAll(foundInRepo);
          monitor.worked(5);
        }
      }
    }
    monitor.done();
    return new Result<Set<File>, Set<IInstallableUnit>>(files, missingIU);
  }

  private Set<File> findInRepo(IArtifactKey a) {
    HashSet<File> files = new HashSet<File>();
    for (IFileArtifactRepository fileRepo : localFileRepo) {
      IArtifactDescriptor[] artifactDescriptors = fileRepo.getArtifactDescriptors(a);
      for (IArtifactDescriptor descriptor : artifactDescriptors) {
        files.add(fileRepo.getArtifactFile(descriptor));
      }
    }
    return files;
  }
  public void reloadLocalRepo(IProgressMonitor monitor) throws ProvisionException {
    localFileRepo = getLocalFileRepoInternal(monitor);
  }
  public void install( Set<IInstallableUnit> toInstall, IProgressMonitor mon) throws ProvisionException {
    // see org.eclipse.equinox.internal.p2.director.app.DirectorApplication.performProvisioningActions()
    SubMonitor monitor = SubMonitor.convert(mon, "install", 1000);
    // Creating an operation
    ProvisioningSession session          = new ProvisioningSession(agent);
    InstallOperation    installOperation = new InstallOperation(session, toInstall);
    installOperation.setProfileId(getProfile());

    ProvisioningContext context = new ProvisioningContext(agent);
    installOperation.setProvisioningContext(context);

    monitor.checkCanceled();
    if (installOperation.resolveModal(monitor.split(100)).isOK()) {
      try {
        ProvisioningJob job = installOperation.getProvisioningJob(monitor.split(100));

        job.runModal(monitor.split(600));
        job.join();
        monitor.checkCanceled();

      } catch (InterruptedException e) {
        Utils.sneakyThrow(e);
      }
    }

    monitor.done();
  }

  private String getProfile() throws ProvisionException {
    IProfileRegistry    profileRegistry  = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
    String              profile          = PROFILE;

    if (profileRegistry.getProfile(profile) == null) {
      Map<String, String> props = new HashMap<>();
      props.put("org.eclipse.equinox.p2.roaming", "true");
      profileRegistry.addProfile(profile, props);

    }
    return profile;
  }

}
