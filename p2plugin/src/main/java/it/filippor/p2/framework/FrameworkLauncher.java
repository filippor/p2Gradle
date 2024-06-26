package it.filippor.p2.framework;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import it.filippor.p2.util.Extensions;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.wiring.BundleRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.osgi.internal.framework.EquinoxConfiguration;

/**
 * Utility class to launch framework
 * @author filippo.rossoni
 */
public class FrameworkLauncher {
  private static final Logger logger = LoggerFactory.getLogger(FrameworkLauncher.class);

  private final File frameworkStorage;

  private final Collection<String> extraSystemPackage;

  private final Iterable<String> startBundlesSymbolicNames;

  private final Iterable<File> bundles;

  private transient File tempSecureStorage;

  /**
   * Constructor
   * @param frameworkStorage location of osgi framework storage
   * @param extraSystemPackage package to share between gradle and osgi
   * @param startBundlesSymbolicNames bundles to start
   * @param bundles bundles to install in osgi framework
   */
  public FrameworkLauncher(final File frameworkStorage, final Collection<String> extraSystemPackage,
                           final Iterable<String> startBundlesSymbolicNames, final Iterable<File> bundles) {
    super();
    this.frameworkStorage          = frameworkStorage;
    this.extraSystemPackage        = extraSystemPackage;
    this.startBundlesSymbolicNames = startBundlesSymbolicNames;
    this.bundles                   = bundles;
  }

  /**
   *  start the framework
   */
  public void startFramework() {
    try {
      this.setInitialProperty();
      if ((!EclipseStarter.isRunning())) {
          EclipseStarter.startup(this.getNonFrameworkArgs(), null);

      }

      BundleContext ctx = EclipseStarter.getSystemBundleContext();
      if ((ctx == null)) {
        throw new RuntimeException("systemBundleContext is null");
      }
      for (String sn : startBundlesSymbolicNames) {
        tryActivateBundle(ctx, sn);
      }
      checkAllBundles(ctx);
    } catch (SecurityException e) {
      logger.error("{}\n try to apply this plugin before others \n"
          + "biz.aQute.bnd.builder version 5.3.0 has conflict with org.osgi.service.log.LogLevel class but work fine if apllied after it.filippor.p2",e.getMessage());
      throw Extensions.sneakyThrow(e);
    } catch (Exception e) {
      throw Extensions.sneakyThrow(e);
    }

  }

  /**
   * execute action with a service provider
   * @param action execute action
   */
  public void executeWithServiceProvider(final Consumer<ServiceProvider> action) {
    if(!isStarted()) {
      logger.warn("framework is not started");
      startFramework();
    }
    BundleContext bctx = EclipseStarter.getSystemBundleContext();
    ServiceProvider serviceProvider = new ServiceProvider(bctx);
    action.accept(serviceProvider);
    serviceProvider.ungetAll();
  }

  /**
   * terminate the framework
   */
  public void stopFramework() {
    try {
      EclipseStarter.shutdown();
      if (this.tempSecureStorage != null) {
        if (!this.tempSecureStorage.delete()) {
          logger.warn("cannot delete {}", tempSecureStorage);
        }
      }
    } catch (Exception e) {
      throw Extensions.sneakyThrow(e);
    }
  }

  private void setInitialProperty() throws MalformedURLException {
    HashMap<String, String> props = new HashMap<>();
    props.put(EquinoxConfiguration.PROP_USE_SYSTEM_PROPERTIES, "false");
    props.put(EquinoxConfiguration.PROP_CONTEXTCLASSLOADER_PARENT, EquinoxConfiguration.CONTEXTCLASSLOADER_PARENT_FWK);
    props.put(EclipseStarter.PROP_INSTALL_AREA, frameworkStorage.toPath().resolve("install").toUri().toURL().toString());
    props.put(EclipseStarter.PROP_SYSPATH, frameworkStorage.toPath().resolve("sysPath").toUri().toURL().toString());
    props.put(Constants.FRAMEWORK_STORAGE, frameworkStorage.toPath().resolve("storage").toUri().toURL().toString());
    props.put(EclipseStarter.PROP_BUNDLES,
              StreamSupport.stream(bundles.spliterator(), false).map(this::toBundleId).collect(Collectors.joining(",")));
    props.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, String.join(";", this.extraSystemPackage));
    EclipseStarter.setInitialProperties(props);

  }

  private void tryActivateBundle(final BundleContext ctx, final String symbolicName) {
    AtomicBoolean found = new AtomicBoolean(false);
    for (Bundle bundle : ctx.getBundles()) {
      if (isNotFragment(bundle)) {
        if (symbolicName.equals(bundle.getSymbolicName())) {
          try {
            logger.info("start {}", bundle);
            bundle.start(Bundle.START_TRANSIENT);
            return;
          } catch (final BundleException e) {
            logger.warn("Could not start bundle {} {}", bundle.getSymbolicName(), e.getMessage());
          }
        }
      }
    }

    if (!found.get()) {
      throw new RuntimeException(symbolicName + " not found");
    }
  }

  private String toBundleId(final File file) {
    try {
      return ("reference:" + file.toURI().toURL());
    } catch (MalformedURLException e) {
      throw Extensions.sneakyThrow(e);
    }
  }

  private String[] getNonFrameworkArgs() {
    try {
      final File tmp = File.createTempFile("p2store", "secure_storage");
      this.tempSecureStorage = tmp;
      tmp.deleteOnExit();
      final List<String> nonFrameworkArgs = new ArrayList<>( List.of("-eclipse.keyring", tmp.getAbsolutePath()));
      if (FrameworkLauncher.logger.isDebugEnabled()) {
        nonFrameworkArgs.add("-debug");
        nonFrameworkArgs.add("-consoleLog");
      }
      return (nonFrameworkArgs.toArray(new String[nonFrameworkArgs.size()]));
    } catch (final IOException e) {
      throw new RuntimeException(("Could not create P2 secure store file in temp dir " + System.getProperty("java.io.tmpdir")),
                                 e);

    }
  }

  private void checkAllBundles(final BundleContext ctx) {
    Arrays.stream(ctx.getBundles()).filter(FrameworkLauncher::isNotFragment).filter(b -> {
      logger.info("[{}]{} {} v:{}", b.getBundleId(), FrameworkLauncher.formatState(b), b.getSymbolicName(),b.getVersion());
      return !Arrays.asList(Bundle.RESOLVED, Bundle.STARTING, Bundle.ACTIVE).contains(b.getState());
    }).forEach((Bundle b) -> {
      try {
        b.start(Bundle.START_TRANSIENT);
      } catch (final BundleException e) {
        FrameworkLauncher.logger.warn("failed to resolve {} {}, {}", FrameworkLauncher.formatState(b), b, e.getMessage());
      }
    });
  }

//  public static ServiceReference<?>[] formatServices(final Bundle bundle) {
//    return bundle.getRegisteredServices();
//  }

  private static String formatState(final Bundle it) {
    final String _switchResult;
    switch (it.getState()) {
      case Bundle.UNINSTALLED:
        _switchResult = "UNINSTALLED";
        break;
      case Bundle.INSTALLED:
        _switchResult = "INSTALLED";
        break;
      case Bundle.RESOLVED:
        _switchResult = "RESOLVED";
        break;
      case Bundle.STARTING:
        _switchResult = "STARTING";
        break;
      case Bundle.STOPPING:
        _switchResult = "STOPPING";
        break;
      case Bundle.ACTIVE:
        _switchResult = "ACTIVE";
        break;
      default:
        throw new IllegalStateException("bundle " + it + "in invalid state " + it.getState());
    }
    return _switchResult;
  }

  private static boolean isNotFragment(final Bundle bundle) {
    return (bundle.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) == 0;
  }

  /**
   * get framework status
   * @return  framework is running
   */
  public boolean isStarted() {
    return EclipseStarter.isRunning();
  }



  @Override
  public int hashCode() {
    final int prime  = 31;
    int       result = 1;
    result = prime * result + ((this.frameworkStorage == null) ? 0 : this.frameworkStorage.hashCode());
    result = prime * result + ((this.extraSystemPackage == null) ? 0 : this.extraSystemPackage.hashCode());
    result = prime * result + ((this.startBundlesSymbolicNames == null) ? 0 : this.startBundlesSymbolicNames.hashCode());
    return prime * result + ((this.bundles == null) ? 0 : this.bundles.hashCode());
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
        return true;
    }
    if ((obj == null) || (getClass() != obj.getClass())) {
        return false;
    }
    FrameworkLauncher other = (FrameworkLauncher) obj;
    if (this.frameworkStorage == null) {
      if (other.frameworkStorage != null) {
        return false;
    }
    } else if (!this.frameworkStorage.equals(other.frameworkStorage)) {
        return false;
    }
    if (this.extraSystemPackage == null) {
      if (other.extraSystemPackage != null) {
        return false;
    }
    } else if (!this.extraSystemPackage.equals(other.extraSystemPackage)) {
        return false;
    }
    if (this.startBundlesSymbolicNames == null) {
      if (other.startBundlesSymbolicNames != null) {
        return false;
    }
    } else if (!this.startBundlesSymbolicNames.equals(other.startBundlesSymbolicNames)) {
        return false;
    }
    if (this.bundles == null) {
      return other.bundles == null;
    } else {
        return this.bundles.equals(other.bundles);
    }
  }


}
