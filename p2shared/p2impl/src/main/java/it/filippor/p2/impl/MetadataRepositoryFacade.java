package it.filippor.p2.impl;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.expression.ExpressionUtil;
import org.eclipse.equinox.p2.metadata.expression.IExpression;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.framework.VersionRange;

import it.filippor.p2.api.Bundle;
import it.filippor.p2.impl.util.LazyProvider;
import it.filippor.p2.impl.util.LazyProvider.Provider;

public class MetadataRepositoryFacade {

  IProvisioningAgent                 agent;
  private IMetadataRepositoryManager manager;
  Set<Supplier<IMetadataRepository>> repos;

  public static final IExpression matchesRequirementsExpression = ExpressionUtil.parse("$0.exists(r | this ~= r)");

  public MetadataRepositoryFacade(IProvisioningAgent agent,Iterable<URI> sites, SubMonitor mon) {
    this.agent      = agent;
    manager         = (IMetadataRepositoryManager) agent.getService(IMetadataRepositoryManager.SERVICE_NAME);
    setUpdateSite(sites, mon);
  }

  private void setUpdateSite(Iterable<URI> sites, SubMonitor mon) {
    repos = new HashSet<>();
    for (URI site : sites) {
      manager.addRepository(site);
      Supplier<IMetadataRepository> metadataRepo = new LazyProvider<IMetadataRepository>((Provider<IMetadataRepository>) () -> manager
        .loadRepository(site, mon.split(200)));
      repos.add(metadataRepo);
    }
  }

  public String getReposAsString() {
    return toString(repos.stream().map(r -> r.get()));
  }
  
  
  
  private IQuery<IInstallableUnit> getRequirementsQuery(IInstallableUnit iu) {
    return QueryUtil.createMatchQuery(MetadataRepositoryFacade.matchesRequirementsExpression, iu.getRequirements());
  }

  public Set<IInstallableUnit> findMetadata(Collection<Bundle> bundles, boolean transitive, SubMonitor mon) {
    Set<IInstallableUnit> toInstall = bundles.stream().flatMap(bundle -> {
      IQuery<IInstallableUnit>              iuQuery = QueryUtil.createIUQuery(bundle.getId(), toVersion(bundle.getVersion()));
      Stream<Supplier<IMetadataRepository>> test    = repos.parallelStream();

      Optional<Set<IInstallableUnit>> ius = test.map(r -> {
        Set<IInstallableUnit> found = r.get().query(iuQuery, mon.split(50)).toSet();
        if (transitive && !found.isEmpty()) {
          List<IQuery<IInstallableUnit>> queries = found.parallelStream()
            .map(iu -> getRequirementsQuery(iu))
            .collect(Collectors.toList());
          found.addAll(r.get().query(QueryUtil.createCompoundQuery(queries, false), mon.split(50)).toSet());
        }
        return found;
      }).filter(set -> !set.isEmpty()).findAny();

      if (ius.isEmpty())
        throw new IllegalArgumentException(bundle + " not found serching in p2 repositories :" + getReposAsString());

      return ius.get().stream();
    }).collect(Collectors.toSet());
    return toInstall;
  }
  
  private org.eclipse.equinox.p2.metadata.VersionRange toVersion(VersionRange version) {
    return org.eclipse.equinox.p2.metadata.VersionRange.create(version.toString());
  }

  private String toString(Stream<IMetadataRepository> repos) {
    return "\n\t" + repos.map(r -> r.getLocation().toString()).collect(Collectors.joining("\n\t"));
  }
  
}
