package it.filippor.p2.impl;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import it.filippor.p2.api.Bundle;
import it.filippor.p2.api.DefaultRepo;
import it.filippor.p2.api.P2RepositoryManager;
import it.filippor.p2.api.ProgressMonitor;
import it.filippor.p2.impl.util.Result;
import it.filippor.p2.impl.util.Utils;

@Component()
public class P2RepositoryManagerImpl implements P2RepositoryManager {

  BundleContext ctx;

  @Activate
  public void activate(BundleContext ctx) {
    this.ctx = ctx;
  }

  @Override
  public Set<File> resolve(DefaultRepo repo, Iterable<URI> sites, Collection<Bundle> bundles, boolean transitive,
                           ProgressMonitor monitor) {

    IProgressMonitor wrappedMonitor = WrappedMonitor.wrap(monitor);

    IProvisioningAgent agent = null;
    try {
      SubMonitor mon = SubMonitor.convert(wrappedMonitor, "resolve", 1000);

      agent = getAgent(repo);
      MetadataRepositoryFacade repoContext = new MetadataRepositoryFacade(agent, sites, mon.split(100));
      Set<IInstallableUnit>    toInstall   = repoContext.findMetadata(bundles, transitive, mon.split(400));

      ArtifactRepositoryFacade artifactRepo = new ArtifactRepositoryFacade(agent, sites, mon.split(100));
      Set<File>                files        = artifactRepo.getFiles(toInstall, mon.split(400));

      return files;
    } catch (Exception e) {
      // e.printStackTrace();
      Utils.sneakyThrow(e);
      return null;
    } finally {
      if (agent != null)
        agent.stop();
    }
  }

  private IProvisioningAgent getAgent(DefaultRepo repo) throws ProvisionException {
    ServiceReference<?> sr = ctx.getServiceReference(IProvisioningAgentProvider.SERVICE_NAME);
    if (sr == null)
      throw new IllegalStateException("cannot find agent provider");
    IProvisioningAgentProvider agentProvider = (IProvisioningAgentProvider) ctx.getService(sr);
    IProvisioningAgent         agent         = agentProvider.createAgent(repo.getAgentURI());
    // IProvisioningAgent agent = agentProvider.createAgent(new File("/home/filippor/.p2/").toURI());
    return agent;
  }

}
