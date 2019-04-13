package it.filippor.p2.impl;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
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
  BundleContext ctx;

  @Activate
  public void activate(BundleContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Object resolve(DefaultRepo repo, String site, String artifactId, String version) {

    try {
      IProvisioningAgent agent = getAgent(repo);

      // get the repository managers
      IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) agent
        .getService(IArtifactRepositoryManager.SERVICE_NAME);

      // define our repositories
//      artifactManager.createRepository(repo.getRepoUri(), "localRepo", "org.eclipse.equinox.p2.artifact.repository.simpleRepository", null);
      artifactManager.addRepository(URI.create(site));
      artifactManager.loadRepository(URI.create(site), new NullProgressMonitor());

      // Load and query the metadata
      IMetadataRepositoryManager manager         = (IMetadataRepositoryManager) agent
          .getService(IMetadataRepositoryManager.SERVICE_NAME);
      manager.addRepository(URI.create(site));
      IMetadataRepository   metadataRepo = manager.loadRepository(URI.create(site), new NullProgressMonitor());
      Set<IInstallableUnit> toInstall    = metadataRepo.query(QueryUtil.createIUQuery(artifactId), new NullProgressMonitor())
        .toUnmodifiableSet();

      // Creating an operation
      CompletableFuture<IStatus> future = new CompletableFuture<>();
      InstallOperation installOperation = new InstallOperation(new ProvisioningSession(agent), toInstall);
      IProfileRegistry profileRegistry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
      if(profileRegistry.getProfile(PROFILE)==null)
        profileRegistry.addProfile(PROFILE);
      installOperation.setProfileId(PROFILE);
      if (installOperation.resolveModal(new NullProgressMonitor()).isOK()) {
        Job job = installOperation.getProvisioningJob(new NullProgressMonitor());
        job.addJobChangeListener(new JobChangeAdapter() {
          public void done(IJobChangeEvent event) {
            future.complete(event.getResult());
            agent.stop();
          }
        });
        job.schedule();
      }
      // toInstall.forEach(iu -> {
      // System.out.println(iu);
      // iu.getArtifacts().forEach(ar -> {
      // IArtifactDescriptor[] artifactDescriptors = artifactRepo.getArtifactDescriptors(ar);
      // for (IArtifactDescriptor ad : artifactDescriptors) {
      // System.out.println(ad.getProperties());
      // System.out.println(ad);
      //// OutputStream destination = System.out;
      //// IStatus status = artifactRepo.getArtifact(ad, destination, new NullProgressMonitor());
      //// System.out.println("Status " + status);
      // }
      //// IQuery<IArtifactKey> query = new ArtifactKeyQuery(ar);
      //// IQueryResult<IArtifactKey> result = artifactManager.query(query, new NullProgressMonitor());
      //// result.forEach(res -> {
      //// System.out.println("\t"+res);
      //// });
      // });
      // });

      return future.get();
    } catch (ProvisionException | InterruptedException | ExecutionException e) {
      e.printStackTrace();
      sneakyThrow(e);
      return null;
    }
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
