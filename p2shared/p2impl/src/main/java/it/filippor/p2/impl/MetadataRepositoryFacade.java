package it.filippor.p2.impl;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.framework.VersionRange;

import it.filippor.p2.api.Bundle;
import it.filippor.p2.impl.util.LazyProvider;

public class MetadataRepositoryFacade {

  private final IMetadataRepositoryManager       manager;
  private Set<LazyProvider<IMetadataRepository>> repos;

  // public static final IExpression matchesRequirementsExpression = ExpressionUtil.parse("$0.exists(r | this ~= r)");

  public MetadataRepositoryFacade(IProvisioningAgent agent, Iterable<URI> sites, SubMonitor mon) {
    manager = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
    setUpdateSite(sites);
  }

  private void setUpdateSite(Iterable<URI> sites) {
    repos = new HashSet<>();
    for (URI site : sites) {
      manager.addRepository(site);
      LazyProvider<IMetadataRepository> metadataRepo = new LazyProvider<>(monitor -> manager.loadRepository(site, monitor));
      repos.add(metadataRepo);
    }
  }

  private String getReposAsString(IProgressMonitor mon) {
    return toString(repos.stream().map(r -> r.get(mon)));
  }

  public Set<IInstallableUnit> findMetadata(Collection<Bundle> bundles/* , boolean transitive */, IProgressMonitor monitor) {
    SubMonitor mon = SubMonitor.convert(monitor, "find metadata", 1000 * bundles.size());

    return bundles.stream().flatMap(bundle -> {
      IQuery<IInstallableUnit> iuQuery = QueryUtil.createIUQuery(bundle.getId(), toVersion(bundle.getVersion()));

      Optional<Set<IInstallableUnit>> ius = repos.parallelStream()
        .map(r -> r.get(mon.split(500)).query(iuQuery, mon.split(250)).toSet())
        .filter(set -> !set.isEmpty())
        .findAny();

      if (!ius.isPresent())
        throw new IllegalArgumentException(bundle + " not found serching in p2 repositories :" + getReposAsString(mon.split(10)));

      return ius.get().stream();
    }).collect(Collectors.toSet());
  }

  private org.eclipse.equinox.p2.metadata.VersionRange toVersion(VersionRange version) {
    return org.eclipse.equinox.p2.metadata.VersionRange.create(version.toString());
  }

  private String toString(Stream<IMetadataRepository> repos) {
    return "\n\t" + repos.map(r -> r.getLocation().toString()).collect(Collectors.joining("\n\t"));
  }

}
