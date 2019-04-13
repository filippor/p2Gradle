package it.filippor.p2.impl;

import java.net.URI;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
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
      IMetadataRepositoryManager manager         = (IMetadataRepositoryManager) agent
        .getService(IMetadataRepositoryManager.SERVICE_NAME);
      IArtifactRepositoryManager artifactManager = (IArtifactRepositoryManager) agent
        .getService(IArtifactRepositoryManager.SERVICE_NAME);

      // define our repositories
//      manager.createRepository(repo.getRepoUri(), "localRepo", "simpleRepository", null);
//      manager.addRepository(repo.getRepoUri());
//      artifactManager.addRepository(repo.getRepoUri());

      // Load and query the metadata
      IMetadataRepository   metadataRepo = manager.loadRepository(URI.create(site) , new NullProgressMonitor());
      Set<IInstallableUnit> toInstall    = metadataRepo
        .query(QueryUtil.createIUQuery(artifactId), new NullProgressMonitor())
        .toUnmodifiableSet();

      return toInstall;
    } catch (ProvisionException e) {
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
