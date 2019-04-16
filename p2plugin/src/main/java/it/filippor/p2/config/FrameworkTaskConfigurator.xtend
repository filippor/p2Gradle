package it.filippor.p2.config

import it.filippor.p2.framework.FrameworkLauncher
import it.filippor.p2.framework.ServiceProvider
import java.io.File
import java.util.function.BiConsumer
import org.gradle.api.Project
import org.gradle.api.Task

class FrameworkTaskConfigurator {
	val public static P2_FRAMEWORK_BUNDLES_CONFIG = "p2frameworkBundles"
	val public static P2_START_FRAMEWORK_TASK = "p2startFramework"
	val public static P2_STOP_FRAMEWORK_TASK = "p2stopFramework"

	val Task stopFrameworkTask

	val Task startFrameworkTask

	val FrameworkLauncher p2FrameworkLauncher
	
	new(Project project) {
		val prj = project.rootProject

		val frameworkBundles = prj.configurations.findByName(P2_FRAMEWORK_BUNDLES_CONFIG) ?: {
			val config = prj.configurations.create(P2_FRAMEWORK_BUNDLES_CONFIG)
			prj.dependencies.add(P2_FRAMEWORK_BUNDLES_CONFIG, "it.filippor.p2:p2impl:0.0.1")
			config
		}

		p2FrameworkLauncher = createFrameworkLauncher(prj,frameworkBundles)

		stopFrameworkTask = prj.tasks.findByName(P2_STOP_FRAMEWORK_TASK) ?: prj.tasks.register(P2_STOP_FRAMEWORK_TASK) [
			doLast([
				p2FrameworkLauncher.stopFramework()
			])
		].get


		
		startFrameworkTask = prj.tasks.findByName(P2_START_FRAMEWORK_TASK) ?:
			prj.tasks.register(P2_START_FRAMEWORK_TASK) [
				finalizedBy(stopFrameworkTask)
				doLast[
					p2FrameworkLauncher.startFramework()
				]
			].get
	}

	def createFrameworkLauncher(Project project,Iterable<File> bundles) {
		val frameworkStoragePath = project.buildDir.toPath.resolve("tmp").resolve("p2Framework").toFile
		val p2ApiPackage = #{"it.filippor.p2.api"}
		val startBundlesSymbolicNames = #["org.eclipse.equinox.ds", "org.eclipse.equinox.registry",
			"org.eclipse.core.net", "org.apache.felix.scr", "p2impl"]
		return new FrameworkLauncher(frameworkStoragePath, p2ApiPackage, startBundlesSymbolicNames,bundles)
	}

	def doLastOnFramework(Task task, BiConsumer<Task,ServiceProvider> action) {
		task.dependsOn += startFrameworkTask
		stopFrameworkTask.mustRunAfter(stopFrameworkTask.mustRunAfter, task)

		task.doLast [ t |
			if(!p2FrameworkLauncher.isStarted()){
				t.logger.warn("framework is not running")
				p2FrameworkLauncher.startFramework()
			}
			
			p2FrameworkLauncher.executeWithServiceProvider[
				action.accept(t,it)
			]
		]

	}
}