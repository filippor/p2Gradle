package it.filippor.p2.impl.samples;

import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.IInstallableUnitPatch;
import org.eclipse.equinox.p2.metadata.expression.ExpressionUtil;
import org.eclipse.equinox.p2.metadata.expression.IExpression;
import org.eclipse.equinox.p2.metadata.expression.IExpressionFactory;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.QueryUtil;

public class P2Discovery {

  public void testQuery() {
    String                id    = null, classifier = null, range = null, key = null, value = null, v1 = null, v2 = null,
        v3 = null;
    IInstallableUnit      iu    = null;
    IInstallableUnitPatch patch = null;
    {
      // Here are some examples of how to use the expressions with IQuery: Query for all IU's that has an id:

      IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery("id == $0", id);
    }
    {
      // Query for the latest IU of some specific id:

      IQuery<IInstallableUnit> query = QueryUtil.createQuery("latest(x | x.id == $0)", id);
    }
    {
      // Query an artifact repository for all keys with a specific classifier:

      IQuery<IArtifactKey> query = QueryUtil.createMatchQuery(IArtifactKey.class, "classifier == $0", classifier);
    }
    {
      // Query for the latest IU that matches a specific version range. Since the second parameter is a VersionRange,
      // the ~= operator is interpreted as isIncluded:

      IQuery<IInstallableUnit> query = QueryUtil.createQuery("latest(x | x.id == $0 && x.version ~= $1)", id, range);
    }
    {
      // Query for an IU that has a specific property set:

      IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery("properties[$0] == $1", key, value);
    }
    {
      // The same query, but this time for multiple possible values:

      IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery("$1.exists(v | properties[$0] == v)", key,
                                                                  new Object[] { v1, v2, v3 });
    }
    {
      // Query for all categories found in the repository:

      IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery("properties[$0] == true", IInstallableUnit.PROP_PARTIAL_IU);
    }
    {
      // Query for all IU's that fulfil at least one of the requirements from another IU. Since the first parameter is a
      // list of IRequirements, the ~= applied to each each IU using satisfies.

      IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery("$0.exists(rc | this ~= rc)", iu.getRequirements());
    }
    {
      // Query for the latest version of all patches:

      IQuery<IInstallableUnit> query = QueryUtil.createQuery("latest(x | x ~= $0)", IInstallableUnitPatch.class);
    }
    {
      // Query for all IU's affected by a patch:

      IQuery<IInstallableUnit> query = QueryUtil.createMatchQuery("$0.exists(rcs | rcs.all(rc | this ~= rc))",
                                                                  (Object)patch.getApplicabilityScope());
    }
  }

  private void testJavaApi() {
    // Create some expressions. Note the use of identifiers instead of
    // indexes for the parameters

    IExpressionFactory factory = ExpressionUtil.getFactory();
    IExpression        item    = factory.variable("this");

    IExpression cmp1 = factory.equals(factory.member(item, "id"), factory.indexedParameter(0));
    IExpression cmp2 = factory.equals(factory.at(factory.member(item, "properties"), factory.indexedParameter(1)),
                                      factory.indexedParameter(2));

    IExpression everything = factory.variable("everything");
    IExpression lambda     = factory.lambda(item, factory.and(new IExpression[] { cmp1, cmp2 }));
    IExpression latest     = factory.latest(factory.select(everything, lambda));

    // Create the query
    IQuery<IInstallableUnit> query = QueryUtil.createQuery(latest, "test.bundle", "org.eclipse.equinox.p2.type.group",
                                                           Boolean.TRUE);

  }
  // private void test() {
  // Catalog catalog = new Catalog();
  // catalog.setEnvironment(DiscoveryCore.createEnvironment());
  // catalog.setVerifyUpdateSiteAvailability(false);
  //
  // // add strategy for retrieving remote catalog
  // RepositoryDiscoveryStrategy strategy = new RepositoryDiscoveryStrategy();
  // strategy.addLocation(new URI("http://location/of/p2/repo"));
  // catalog.getDiscoveryStrategies().add(strategy);
  //
  // CatalogConfiguration configuration = new CatalogConfiguration();
  // configuration.setShowTagFilter(false);
  //
  // DiscoveryWizard wizard = new DiscoveryWizard(catalog, configuration);
  // WizardDialog dialog = new WizardDialog(WorkbenchUtil.getShell(), wizard);
  // dialog.open();
  //
  // }
  //
  // private void testDirectory() {
  // Catalog catalog = new Catalog();
  // catalog.setEnvironment(DiscoveryCore.createEnvironment());
  // catalog.setVerifyUpdateSiteAvailability(false);
  //
  // // look for descriptors from installed bundles
  // catalog.getDiscoveryStrategies().add(new BundleDiscoveryStrategy());
  //
  // // look for remote descriptor
  // RemoteBundleDiscoveryStrategy remoteDiscoveryStrategy = new RemoteBundleDiscoveryStrategy();
  // remoteDiscoveryStrategy.setDirectoryUrl("http://location/of/directory.xml");
  // catalog.getDiscoveryStrategies().add(remoteDiscoveryStrategy);
  //
  // CatalogConfiguration configuration = new CatalogConfiguration();
  // configuration.setShowTagFilter(false);
  //
  // DiscoveryWizard wizard = new DiscoveryWizard(catalog, configuration);
  // WizardDialog dialog = new WizardDialog(WorkbenchUtil.getShell(), wizard);
  // dialog.open();
  //
  // }
}
