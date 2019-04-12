package it.filippor.p2

import java.io.File
import java.io.Serializable
import java.util.HashMap
import java.util.Map
import java.util.ServiceLoader
import java.util.function.Consumer
import org.eclipse.xtend.lib.annotations.Data
import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.Constants
import org.osgi.framework.launch.Framework
import org.osgi.framework.launch.FrameworkFactory
import org.osgi.framework.wiring.BundleRevision
import org.slf4j.LoggerFactory

@Data
class FrameworkLauncher implements Serializable {
	val static logger = LoggerFactory.getLogger(FrameworkLauncher)
	val File frameworkStorage
	val Iterable<String> extraSystemPackage
		

	def void createFramework(Iterable<File> bundles, Iterable<File> bundlesToRun) {
		instantiateFramework() => [
			try {
				init()
				start()
				bundleContext => [
					installBundles(bundles)
					startBundles(bundlesToRun)
					if(logger.isWarnEnabled) checkAllBundles()
				]
			} finally {
				stop()
				waitForStop(10000)
			}
		]
	}

	def <T> void executeWithService(Class<T> clazz, Consumer<T> action) {
		executeWithServiceProvider() [
			action.accept(getService(clazz))
		]
	}

	def void executeWithServiceProvider(Consumer<ServiceProvider> action) {
		instantiateFramework() => [
			try {
				start()
				var ServiceProvider serviceProvider = new ServiceProvider(bundleContext)
				action.accept(serviceProvider)
				serviceProvider.ungetAll()
			} finally {
				stop()
				waitForStop(10000)
			}
		]
	}

	def private Framework instantiateFramework() {
		var ServiceLoader<FrameworkFactory> ffs = ServiceLoader.load(FrameworkFactory)
		var FrameworkFactory ff = ffs.iterator().next()
		val Map<String, String> config = new HashMap()
		config.put(Constants.FRAMEWORK_STORAGE, frameworkStorage.toURI.toURL.toString)
		config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, extraSystemPackage.join(";"))
		return ff.newFramework(config)
	}

	def private void startBundles(BundleContext ctx, Iterable<File> startBundlesFile) {
		startBundlesFile.map[toURI.toURL.toString].map [ bf |
			ctx.getBundle(bf) ?: ctx.installBundle(bf)
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
			! #[Bundle.RESOLVED, Bundle.STARTING, Bundle.ACTIVE].contains(b.state)
		].forEach [ b |
			try {
				b.start(Bundle.START_TRANSIENT)
			} catch (Exception e) {
				logger.warn("failed to resolve {} {}, {}", b.formatState, b, e.message)
			}
		]
	}

	def private void installBundles(BundleContext ctx, Iterable<File> bundleFiles) {
		bundleFiles.forEach [ bf |
			try {
				val b = ctx.installBundle(bf.toURI.toURL.toString.trim)
				logger.info("installed status: {}, bundle: {}", b.formatState, b)
			} catch (Exception e) {
				logger.warn("failed install {} error:{}", bf.toURI.toURL.toString, e.message)
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
