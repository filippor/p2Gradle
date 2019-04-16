package it.filippor.p2.framework

import java.io.File
import java.io.IOException
import java.io.Serializable
import java.util.function.Consumer
import org.eclipse.core.runtime.adaptor.EclipseStarter
import org.eclipse.osgi.internal.framework.EquinoxConfiguration
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
	val Iterable<String> startBundlesSymbolicNames
	val Iterable<File> bundles

	transient File tempSecureStorage

	def void startFramework() {
		setInitialProperty()
		
		if (!EclipseStarter.isRunning)
			EclipseStarter.startup(getNonFrameworkArgs(), null);
		val ctx = EclipseStarter.systemBundleContext
		if (ctx === null)
			logger.error("systemBundleContext is null")

		startBundlesSymbolicNames.forEach [ sn |
			ctx.tryActivateBundle(sn)
		]
		EclipseStarter.systemBundleContext.checkAllBundles()

	}


	def void executeWithServiceProvider(Consumer<ServiceProvider> action) {
		var ServiceProvider serviceProvider = new ServiceProvider(EclipseStarter.systemBundleContext)
		action.accept(serviceProvider)
		serviceProvider.ungetAll()
	}

	def void stopFramework() {
		println("stop framework")
		EclipseStarter.shutdown();
		this.tempSecureStorage?.delete();
	}

	

	def private setInitialProperty() {
		var props = newHashMap(
			EquinoxConfiguration.PROP_USE_SYSTEM_PROPERTIES -> "false",
//			EquinoxConfiguration.PROP_COMPATIBILITY_BOOTDELEGATION -> "false",
			EquinoxConfiguration.PROP_CONTEXTCLASSLOADER_PARENT -> EquinoxConfiguration.CONTEXTCLASSLOADER_PARENT_FWK,
			EclipseStarter.PROP_INSTALL_AREA -> frameworkStorage.toPath.resolve("install").toUri.toURL.toString,
			EclipseStarter.PROP_SYSPATH -> frameworkStorage.toPath.resolve("syspath").toUri.toURL.toString,
			Constants.FRAMEWORK_STORAGE -> frameworkStorage.toPath.resolve("storage").toUri.toURL.toString,
			EclipseStarter.PROP_BUNDLES -> bundles.map[toBundleId].join(","),
			Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA -> extraSystemPackage.join(";")
		)
		EclipseStarter.setInitialProperties(props);

	}

	def private tryActivateBundle(BundleContext ctx, String symbolicName) {
		ctx.bundles.filter[!isFragment].forEach [ bundle |
			if (symbolicName.equals(bundle.getSymbolicName())) {
				try {
					logger.info("start {}", bundle)
					bundle.start(Bundle.START_TRANSIENT); // don't have OSGi remember the autostart
					// setting; want to start these bundles
					// manually to control the start order
				} catch (BundleException e) {
					logger.warn("Could not start bundle {} {}", bundle.getSymbolicName(), e.message);
				}
			}
		]
	}

	def String toBundleId(File file) {
		"reference:" + file.toURI.toURL
//		file.name.replaceAll("[-|_][0-9]*\\.[0-9]*(?:\\.[0-9]*)?.*\\.jar$","")
	}

	def private String[] getNonFrameworkArgs() {
		try {
			val tmp = File.createTempFile("p2store", "secure_storage")
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
			throw new RuntimeException("Could not create P2 secure store file in temp dir " +
				System.getProperty("java.io.tmpdir"), e);
		}
	}

	def private void checkAllBundles(BundleContext ctx) {
		ctx.bundles.filter [
			!isFragment
		].filter [ b |
			logger.info("[{}]{} {}", b.bundleId, b.formatState, b.symbolicName)
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
	
	def isStarted() {
		return EclipseStarter.isRunning
	}

}
