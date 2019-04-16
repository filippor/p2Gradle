package it.filippor.p2

import it.filippor.p2.task.CreateFramework
import java.util.function.BiConsumer
import org.gradle.api.Project
import org.gradle.api.Task

class FrameworkTaskConfigurator {
	val public static P2_FRAMEWORK_BUNDLES_CONFIG = "p2frameworkBundles"

	val Task stopFrameworkTask

	val Task startFrameworkTask

	val FrameworkLauncher p2FrameworkLauncher

	new(Project project) {
		val prj = project.rootProject

		p2FrameworkLauncher = createFrameworkLauncher(prj)

		stopFrameworkTask = prj.tasks.findByName("stopFramework") ?: prj.tasks.register("stopFramework") [
			doLast([
				p2FrameworkLauncher.stopFramework()
			])
		].get

		val frameworkBundles = prj.configurations.findByName(P2_FRAMEWORK_BUNDLES_CONFIG) ?: {
			val config = prj.configurations.create(P2_FRAMEWORK_BUNDLES_CONFIG)
			prj.dependencies.add(P2_FRAMEWORK_BUNDLES_CONFIG, "it.filippor.p2:p2impl:0.0.1")
			config
		}
		startFrameworkTask = prj.tasks.findByName("createFramework") ?:
			prj.tasks.register("createFramework", CreateFramework) [
				finalizedBy(stopFrameworkTask)
				frameworkLauncher = p2FrameworkLauncher
				bundles = frameworkBundles
			].get
	}

	def createFrameworkLauncher(Project project) {
		val frameworkStoragePath = project.buildDir.toPath.resolve("tmp").resolve("p2Framework").toFile
		val p2ApiPackage = #{"it.filippor.p2.api"}
		val startBundlesSymbolicNames = #["org.eclipse.equinox.ds", "org.eclipse.equinox.registry",
			"org.eclipse.core.net", "org.apache.felix.scr", "p2impl"]
		return new FrameworkLauncher(frameworkStoragePath, p2ApiPackage, startBundlesSymbolicNames)
	}

	def doOnFramework(Task task, BiConsumer<FrameworkLauncher, Task> action) {
		task.dependsOn += startFrameworkTask
		stopFrameworkTask.mustRunAfter(stopFrameworkTask.mustRunAfter, task)

		task.doLast [ task1 |
			action.accept(p2FrameworkLauncher, task1)
		]

	}
}
