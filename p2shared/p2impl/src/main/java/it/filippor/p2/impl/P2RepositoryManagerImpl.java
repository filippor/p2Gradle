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
import it.filippor.p2.impl.samples.P2PublisherImpl;
import it.filippor.p2.impl.util.Result;
import it.filippor.p2.impl.util.Utils;

@Component()
public class P2RepositoryManagerImpl implements P2RepositoryManager {

  BundleContext                    ctx;
  private IProvisioningAgent       agent;
  private MetadataRepositoryFacade repoContext;
  private ArtifactRepositoryFacade artifactRepo;

  @Activate
  public void activate(BundleContext ctx) {
    this.ctx = ctx;
  }

  public void init(DefaultRepo repo, Iterable<URI> sites, ProgressMonitor monitor) {
    IProgressMonitor wrappedMonitor = WrappedMonitor.wrap(monitor);
    try {
      SubMonitor mon = SubMonitor.convert(wrappedMonitor, "init", 1000);
      agent        = getAgent(repo);
      repoContext  = new MetadataRepositoryFacade(agent, sites, mon.split(700));
      artifactRepo = new ArtifactRepositoryFacade(agent, sites, mon.split(300));
      mon.done();
    } catch (Exception e) {
      Utils.sneakyThrow(e);
      // } finally {
      // if (agent != null)
      // agent.stop();
    }
  }
  
  public void tearDown() {
    repoContext  = null;
    artifactRepo = null;
    if (agent != null)
       agent.stop();
  }
  
  @Override
  public Set<File> resolve(Collection<Bundle> bundles, boolean transitive,
                           ProgressMonitor monitor) {

    IProgressMonitor wrappedMonitor = WrappedMonitor.wrap(monitor);

    try {
      SubMonitor mon = SubMonitor.convert(wrappedMonitor, "resolve", 1000);
      mon.worked(1);
      Set<IInstallableUnit> toInstall = repoContext.findMetadata(bundles, transitive, mon.split(400));

      Set<File> files = artifactRepo.getFiles(toInstall, mon.split(400));
      mon.done();
      return files;
    } catch (Exception e) {
      Utils.sneakyThrow(e);
      return null;
    }
  }
  
  @Override
  public void publish(URI repo,File[] bundleLocations, ProgressMonitor monitor) {
    IProgressMonitor wrappedMonitor = WrappedMonitor.wrap(monitor);
    try {
      SubMonitor mon = SubMonitor.convert(wrappedMonitor, "init", 1000);
      new P2PublisherImpl().publish(repo, bundleLocations, mon);
    } catch (Exception e) {
      Utils.sneakyThrow(e);
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
