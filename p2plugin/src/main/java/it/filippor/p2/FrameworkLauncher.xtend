package it.filippor.p2

import java.io.File
import java.io.IOException
import java.io.Serializable
import java.util.function.Consumer
import org.eclipse.core.runtime.adaptor.EclipseStarter
import org.eclipse.xtend.lib.annotations.Data
import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.BundleException
import org.osgi.framework.Constants
import org.osgi.framework.wiring.BundleRevision
import org.slf4j.LoggerFactory

@Data
class FrameworkLauncher implements Serializable {
	val static logger = LoggerFactory.getLogger(FrameworkLauncher)
	val File frameworkStorage
	val Iterable<String> extraSystemPackage

	transient File tempSecureStorage

	def void createFramework(Iterable<File> bundles, Iterable<File> bundlesToRun) {
		instantiateFramework(bundles)

		try {
			start()
			val it = EclipseStarter.systemBundleContext
//				installBundles(bundles)
			if(logger.isWarnEnabled) checkAllBundles()
			activateBundlesInWorkingOrder
			startBundles(bundlesToRun)

		} finally {
			stop()
		}

	}

	def <T> void executeWithService(Iterable<File> bundles, Class<T> clazz, Consumer<T> action) {
		executeWithServiceProvider(bundles) [
			action.accept(getService(clazz))
		]
	}

	def void executeWithServiceProvider(Iterable<File> bundles, Consumer<ServiceProvider> action) {
		instantiateFramework(bundles)
		try {
			start()
			var ServiceProvider serviceProvider = new ServiceProvider(EclipseStarter.systemBundleContext)
			action.accept(serviceProvider)
			serviceProvider.ungetAll()
		} finally {
			stop()
		}

	}

	def void stop() {
		EclipseStarter.shutdown();
		this.tempSecureStorage?.delete();
	}

	def start() {
		if (!EclipseStarter.isRunning)
			EclipseStarter.startup(getNonFrameworkArgs(), null);
	}

	def private instantiateFramework(Iterable<File> bundles) {
		var props = newHashMap(
			EclipseStarter.PROP_INSTALL_AREA -> frameworkStorage.toURI.toURL.toString,
			EclipseStarter.PROP_SYSPATH -> frameworkStorage.toPath.resolve("plugin").toUri.toURL.toString,
			// "osgi.configuration.area"->frameworkStorage.toPath.resolve("configuration").toUri.toURL.toString
			EclipseStarter.PROP_BUNDLES -> bundles.map[toBundleId].join(","),
			Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA -> extraSystemPackage.join(";")
		)
		EclipseStarter.setInitialProperties(props);
	}

	def private activateBundlesInWorkingOrder(BundleContext it) {
		// activate bundles which need to do work in their respective activator; stick to a working
		// order (cf. bug 359787)
		// TODO this order should come from the EquinoxRuntimeLocator
		tryActivateBundle("org.eclipse.equinox.ds");
		tryActivateBundle("org.eclipse.equinox.registry");
		tryActivateBundle("org.eclipse.core.net");
	}

	def private tryActivateBundle(BundleContext ctx, String symbolicName) {
		ctx.bundles.forEach [ bundle |
			if (symbolicName.equals(bundle.getSymbolicName())) {
				try {
					bundle.start(Bundle.START_TRANSIENT); // don't have OSGi remember the autostart
					// setting; want to start these bundles
					// manually to control the start order
				} catch (BundleException e) {
					logger.warn("Could not start bundle " + bundle.getSymbolicName(), e);
				}
			}
		]
	}

	def String toBundleId(File file) {
		"reference:" + file.toURI.toURL
//		//TODO: resd from manifest
//		file.name.replaceAll("[-|_][0-9]*\\.[0-9]*(?:\\.[0-9]*)?.*\\.jar$","")
	}

	def private String[] getNonFrameworkArgs() {
		try {
			val tmp = File.createTempFile("tycho", "secure_storage")
			this.tempSecureStorage = tmp;
			tmp.deleteOnExit();

			val nonFrameworkArgs = newArrayList(
				"-eclipse.keyring",
				tmp.getAbsolutePath()
			)

			// TODO nonFrameworkArgs.add("-eclipse.password");
			if (logger.isDebugEnabled()) {
				nonFrameworkArgs.add("-debug");
				nonFrameworkArgs.add("-consoleLog");
			}
			return nonFrameworkArgs
		} catch (IOException e) {
			throw new RuntimeException("Could not create Tycho secure store file in temp dir " +
				System.getProperty("java.io.tmpdir"), e);
		}
	}

	def private void startBundles(BundleContext ctx, Iterable<File> startBundlesFile) {
		startBundlesFile.map [ bf |
			ctx.getBundle(bf.toURI.toURL.toString) ?: ctx.bundles.findFirst[location.endsWith(bf.name)] ?:
				ctx.installBundle(bf.toURI.toURL.toString)
		].filter [
			!isFragment
		].forEach [ b |
			try {
				b.start()
				if (logger.isInfoEnabled) {
					logger.info("started {} {} {}", b, b.formatState, b.formatServices)
				}
			} catch (Exception e) {
				logger.warn("failed to start {} , {}", b, e.message)
			}
		]
	}

	def private void checkAllBundles(BundleContext ctx) {
		ctx.bundles.filter [
			!isFragment
		].filter [ b |
			// println(b.formatState+ b.bundleId + b.location)
			! #[Bundle.RESOLVED, Bundle.STARTING, Bundle.ACTIVE].contains(b.state)
		].forEach [ b |
			try {
				b.start(Bundle.START_TRANSIENT)
			} catch (Exception e) {
				logger.warn("failed to resolve {} {}, {}", b.formatState, b, e.message)
			}
		]
	}

	def static formatServices(Bundle bundle) {
		bundle.registeredServices
	}

	def static formatState(Bundle it) {
		return switch it.state {
			case Bundle.UNINSTALLED: "UNINSTALLED"
			case Bundle.INSTALLED: "INSTALLED"
			case Bundle.RESOLVED: "RESOLVED"
			case Bundle.STARTING: "STARTING"
			case Bundle.STOPPING: "STOPPING"
			case Bundle.ACTIVE: "ACTIVE"
			default: throw new IllegalStateException("bundle " + it + "in invalid state " + it.state)
		}
	}

	def static isFragment(Bundle bundle) {
		return (bundle.adapt(BundleRevision).getTypes().bitwiseAnd(BundleRevision.TYPE_FRAGMENT)) != 0
	}

}
