package it.filippor.p2.framework;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.core.runtime.adaptor.EclipseStarter;
import org.eclipse.osgi.internal.framework.EquinoxConfiguration;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pair;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.wiring.BundleRevision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrameworkLauncher {
  private static final Logger logger = LoggerFactory.getLogger(FrameworkLauncher.class);

  private final File frameworkStorage;

  private final Iterable<String> extraSystemPackage;

  private final Iterable<String> startBundlesSymbolicNames;

  private final Iterable<File> bundles;

  private transient File tempSecureStorage;

  public void startFramework() {
    try {
      this.setInitialProperty();
      if ((!EclipseStarter.isRunning())) {
        EclipseStarter.startup(this.getNonFrameworkArgs(), null);

      }

      if ((EclipseStarter.getSystemBundleContext() == null)) {
        logger.error("systemBundleContext is null");
      }
      this.checkAllBundles(EclipseStarter.getSystemBundleContext());
      for (String sn : startBundlesSymbolicNames) {
        this.tryActivateBundle(EclipseStarter.getSystemBundleContext(), sn);
      }
      this.checkAllBundles(EclipseStarter.getSystemBundleContext());
    } catch (Exception e) {
      Exceptions.sneakyThrow(e);
    }

  }

  public void executeWithServiceProvider(final Consumer<ServiceProvider> action) {
    ServiceProvider serviceProvider = new ServiceProvider(EclipseStarter.getSystemBundleContext());
    action.accept(serviceProvider);
    serviceProvider.ungetAll();
  }

  public void stopFramework() {
    try {
      EclipseStarter.shutdown();
      if (this.tempSecureStorage != null) {
        this.tempSecureStorage.delete();
      }
    } catch (Exception e) {
      Exceptions.sneakyThrow(e);
    }
  }

  private void setInitialProperty() throws MalformedURLException {
    HashMap<String, String> props = new HashMap<>();
    props.put(EquinoxConfiguration.PROP_USE_SYSTEM_PROPERTIES, "false");
    props.put(EquinoxConfiguration.PROP_CONTEXTCLASSLOADER_PARENT, EquinoxConfiguration.CONTEXTCLASSLOADER_PARENT_FWK);
    props.put(EclipseStarter.PROP_INSTALL_AREA, frameworkStorage.toPath().resolve("install").toUri().toURL().toString());
    props.put(EclipseStarter.PROP_SYSPATH, frameworkStorage.toPath().resolve("syspath").toUri().toURL().toString());
    props.put(Constants.FRAMEWORK_STORAGE, frameworkStorage.toPath().resolve("storage").toUri().toURL().toString());
    props.put(EclipseStarter.PROP_BUNDLES,
              StreamSupport.stream(bundles.spliterator(), false).map(this::toBundleId).collect(Collectors.joining(",")));
    props.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, IterableExtensions.join(this.extraSystemPackage, ";"));
    EclipseStarter.setInitialProperties(props);

  }

  private void tryActivateBundle(final BundleContext ctx, final String symbolicName) {
    AtomicBoolean found = new AtomicBoolean(false);
    for (Bundle bundle : ctx.getBundles()) {
      if (!isFragment(bundle)) {
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

  public String toBundleId(final File file) {
    try {
      return ("reference:" + file.toURI().toURL());
    } catch (MalformedURLException e) {
      Exceptions.sneakyThrow(e);
      return null;
    }
  }

  private String[] getNonFrameworkArgs() {
    try {
      final File tmp = File.createTempFile("p2store", "secure_storage");
      this.tempSecureStorage = tmp;
      tmp.deleteOnExit();
      final List<String> nonFrameworkArgs = Arrays.asList("-eclipse.keyring", tmp.getAbsolutePath());
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
    Arrays.asList(ctx.getBundles()).stream().filter(it -> !FrameworkLauncher.isFragment(it)).filter(b -> {
      logger.info("[{}]{} {}", Long.valueOf(b.getBundleId()), FrameworkLauncher.formatState(b), b.getSymbolicName());
      return !Arrays.asList(Bundle.RESOLVED, Bundle.STARTING, Bundle.ACTIVE).contains(b.getState());
    }).forEach((Consumer<Bundle>) (Bundle b) -> {
      try {
        b.start(Bundle.START_TRANSIENT);
      } catch (final BundleException e) {
        FrameworkLauncher.logger.warn("failed to resolve {} {}, {}", FrameworkLauncher.formatState(b), b, e.getMessage());
      }
    });
  }

  public static ServiceReference<?>[] formatServices(final Bundle bundle) {
    return bundle.getRegisteredServices();
  }

  public static String formatState(final Bundle it) {
    String _switchResult = null;
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

  public static boolean isFragment(final Bundle bundle) {
    return (bundle.adapt(BundleRevision.class).getTypes() & BundleRevision.TYPE_FRAGMENT) != 0;
  }

  public boolean isStarted() {
    return EclipseStarter.isRunning();
  }

  public FrameworkLauncher(final File frameworkStorage, final Iterable<String> extraSystemPackage,
                           final Iterable<String> startBundlesSymbolicNames, final Iterable<File> bundles) {
    super();
    this.frameworkStorage          = frameworkStorage;
    this.extraSystemPackage        = extraSystemPackage;
    this.startBundlesSymbolicNames = startBundlesSymbolicNames;
    this.bundles                   = bundles;
  }

  @Override
  @Pure
  public int hashCode() {
    final int prime  = 31;
    int       result = 1;
    result = prime * result + ((this.frameworkStorage == null) ? 0 : this.frameworkStorage.hashCode());
    result = prime * result + ((this.extraSystemPackage == null) ? 0 : this.extraSystemPackage.hashCode());
    result = prime * result + ((this.startBundlesSymbolicNames == null) ? 0 : this.startBundlesSymbolicNames.hashCode());
    return prime * result + ((this.bundles == null) ? 0 : this.bundles.hashCode());
  }

  @Override
  @Pure
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FrameworkLauncher other = (FrameworkLauncher) obj;
    if (this.frameworkStorage == null) {
      if (other.frameworkStorage != null)
        return false;
    } else if (!this.frameworkStorage.equals(other.frameworkStorage))
      return false;
    if (this.extraSystemPackage == null) {
      if (other.extraSystemPackage != null)
        return false;
    } else if (!this.extraSystemPackage.equals(other.extraSystemPackage))
      return false;
    if (this.startBundlesSymbolicNames == null) {
      if (other.startBundlesSymbolicNames != null)
        return false;
    } else if (!this.startBundlesSymbolicNames.equals(other.startBundlesSymbolicNames))
      return false;
    if (this.bundles == null) {
      if (other.bundles != null)
        return false;
    } else if (!this.bundles.equals(other.bundles))
      return false;
    return true;
  }

  @Override
  @Pure
  public String toString() {
    ToStringBuilder b = new ToStringBuilder(this);
    b.add("frameworkStorage", this.frameworkStorage);
    b.add("extraSystemPackage", this.extraSystemPackage);
    b.add("startBundlesSymbolicNames", this.startBundlesSymbolicNames);
    b.add("bundles", this.bundles);
    return b.toString();
  }

  @Pure
  public File getFrameworkStorage() {
    return this.frameworkStorage;
  }

  @Pure
  public Iterable<String> getExtraSystemPackage() {
    return this.extraSystemPackage;
  }

  @Pure
  public Iterable<String> getStartBundlesSymbolicNames() {
    return this.startBundlesSymbolicNames;
  }

  @Pure
  public Iterable<File> getBundles() {
    return this.bundles;
  }
}
