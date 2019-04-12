package it.filippor.p2.impl;

import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
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
  public IProvisioningAgent resolve(DefaultRepo repo, String site, String artifactId, String version) {

    try {
      ServiceReference<?> sr = ctx.getServiceReference(IProvisioningAgentProvider.SERVICE_NAME);
      if (sr == null)
        throw new IllegalStateException("cannot find agent provider");
      IProvisioningAgentProvider agentProvider = (IProvisioningAgentProvider) ctx.getService(sr);
      IProvisioningAgent         agent         = agentProvider.createAgent(repo.getAgentURI());
      return agent;
    } catch (ProvisionException e) {
      sneakyThrow(e);
      return null;
    }
    
  }
  @SuppressWarnings("unchecked")
  public static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
    throw (E) e;
}
}
