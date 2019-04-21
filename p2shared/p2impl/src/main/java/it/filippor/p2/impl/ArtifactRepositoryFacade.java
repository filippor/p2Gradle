package it.filippor.p2.impl;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.engine.ProvisioningContext;
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

import it.filippor.p2.impl.util.Result;
import it.filippor.p2.impl.util.Utils;

public class ArtifactRepositoryFacade {
  IProvisioningAgent                    agent;
  private IArtifactRepositoryManager    artifactManager;
  private List<IFileArtifactRepository> localFileRepo;
  private IProfileRegistry              profileRegistry;
  private Collection<URI> sites;

  public ArtifactRepositoryFacade(IProvisioningAgent agent, Collection<URI> sites, SubMonitor mon) {
    this.sites = sites;
    mon             = SubMonitor.convert(mon, "createArtifactFacade", 20);
    this.agent      = agent;
    artifactManager = (IArtifactRepositoryManager) agent.getService(IArtifactRepositoryManager.SERVICE_NAME);
    profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
//    setUpdateSite(sites, mon.split(10));
    this.localFileRepo = getLocalFileRepo(mon.split(10));
  }

//  private void setUpdateSite(Iterable<URI> sites, SubMonitor mon) {
//    for (URI site : sites) {
//      artifactManager.addRepository(site);
//    }
//  }
  private URI[] getArtifactRepositories() {
    return sites.toArray(new URI[sites.size()]);
  }

  public Set<File> getFiles(Set<IInstallableUnit> toResolve, boolean transitive,
                            SubMonitor monitor) throws ProvisionException, InterruptedException, ExecutionException {
    int totalWork = 100 + (toResolve.size() * 200);
    monitor = SubMonitor.convert(monitor, totalWork);
    if (toResolve.isEmpty())
      return new HashSet<>();

    String profileId = toProfileId(toResolve);
    if (profileRegistry.containsProfile(profileId)) {
      if (transitive) {
        toResolve.addAll(profileRegistry.getProfile(profileId).query(QueryUtil.ALL_UNITS, monitor.split(10)).toSet());
      }
      Result<Set<File>, Set<IInstallableUnit>> result = getFromLocalRepo(toResolve, monitor.split(totalWork));
      if (!result.getMiss().isEmpty()) {
        throw new RuntimeException("can not find artifacts: "
                                   + result.getMiss().stream().map(m -> m.toString()).collect(Collectors.joining(",")));
      }
      monitor.done();
      return result.getHit();
    } else {

      Set<IInstallableUnit> installed = install(toResolve, monitor.split(toResolve.size() * 150));
      reloadLocalRepo(monitor.split(100));
      if (transitive)
        toResolve.addAll(installed);
      Result<Set<File>, Set<IInstallableUnit>> result = getFromLocalRepo(toResolve, monitor.split(toResolve.size() * 50));

      if (!result.getMiss().isEmpty()) {
        throw new RuntimeException("can not find artifacts: "
                                   + result.getMiss().stream().map(m -> m.toString()).collect(Collectors.joining(",")));
      }
      monitor.done();
      return result.getHit();
    }
  }

  private List<IFileArtifactRepository> getLocalFileRepo(IProgressMonitor parentMonitor) {
    URI[]      knownRepositories = artifactManager.getKnownRepositories(IRepositoryManager.REPOSITORIES_LOCAL);
    SubMonitor monitor           = SubMonitor.convert(parentMonitor, "get local repo", 100 * knownRepositories.length);

    List<IFileArtifactRepository> localRepos = Arrays.asList(knownRepositories).stream().map(uri -> {
      try {
        IArtifactRepository loadRepository = artifactManager
          .loadRepository(uri/* ,IRepositoryManager.REPOSITORY_HINT_MODIFIABLE */ , monitor.split(100));
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
    monitor = SubMonitor.convert(monitor, "get from l;ocal repo", toInstall.size() * 10);
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
    SubMonitor mon = SubMonitor.convert(monitor, "refresh artifactRepo", 50 + (10 * localFileRepo.size()));
    for (IFileArtifactRepository iFileArtifactRepository : localFileRepo) {
      artifactManager.refreshRepository(iFileArtifactRepository.getLocation(), mon.split(10));
    }
    localFileRepo = getLocalFileRepo(mon.split(50));
  }

  public Set<IInstallableUnit> install(Set<IInstallableUnit> toInstall, IProgressMonitor mon) throws ProvisionException {
    // see org.eclipse.equinox.internal.p2.director.app.DirectorApplication.performProvisioningActions()
    SubMonitor monitor = SubMonitor.convert(mon, "install", 1000);
    // Creating an operation
    ProvisioningSession session          = new ProvisioningSession(agent);
    
    InstallOperation    installOperation = new InstallOperation(session, toInstall);
    IProfile            profile          = createProfile(toProfileId(toInstall));

    installOperation.setProfileId(profile.getProfileId());

    ProvisioningContext context = new ProvisioningContext(agent);
    context.setArtifactRepositories(getArtifactRepositories());
    installOperation.setProvisioningContext(context);

    monitor.checkCanceled();
    IStatus               status = installOperation.resolveModal(monitor.split(100));
    Set<IInstallableUnit> result = Collections.emptySet();
    if (status.isOK()) {
      try {
        ProvisioningJob job = installOperation.getProvisioningJob(monitor.split(100));

        job.runModal(monitor.split(600));
        job.join();
        monitor.checkCanceled();
        result = installOperation.getProvisioningPlan().getAdditions().query(QueryUtil.ALL_UNITS, monitor.split(10)).toSet();
      } catch (InterruptedException e) {
        Utils.sneakyThrow(e);
      }
    } else {
      throw new RuntimeException(installOperation.getResolutionDetails());
    }
    monitor.done();
    return result;
  }

  

  private IProfile createProfile(String profileName) throws ProvisionException {
    Map<String, String> props = new HashMap<>();
    props.put("org.eclipse.equinox.p2.roaming", "true");
    return profileRegistry.addProfile(profileName, props);
  }

  private String toProfileId(Set<IInstallableUnit> toInstall) {
    String profileName = toInstall.stream().map(iu -> {
      return iu.toString();
    }).collect(Collectors.joining(","));
    return profileName;
  }

}
