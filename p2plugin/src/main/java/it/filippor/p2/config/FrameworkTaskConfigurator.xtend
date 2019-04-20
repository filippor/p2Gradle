package it.filippor.p2.config

import it.filippor.p2.api.Bundle
import it.filippor.p2.api.DefaultRepo
import it.filippor.p2.api.P2RepositoryManager
import it.filippor.p2.framework.FrameworkLauncher
import it.filippor.p2.framework.ServiceProvider
import it.filippor.p2.task.FileProviderTask
import it.filippor.p2.task.PublishTask
import it.filippor.p2.task.TaskWithProgress
import it.filippor.p2.util.ProgressMonitorWrapper
import java.io.File
import java.net.URI
import java.util.Arrays
import java.util.function.BiConsumer
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import it.filippor.p2.task.ResolveTask

class FrameworkTaskConfigurator {
	val public static P2_FRAMEWORK_BUNDLES_CONFIG = "p2frameworkBundles"
	val public static P2_START_FRAMEWORK_TASK = "p2startFramework"
	val public static P2_STOP_FRAMEWORK_TASK = "p2stopFramework"

	val Task stopFrameworkTask

	val Task startFrameworkTask

	val FrameworkLauncher p2FrameworkLauncher

	val Project project

//	val Iterable<URI> updateSites
	new(Project project, Iterable<URI> updateSites) {
		val prj = project.rootProject
		this.project = project
//		this.updateSites = updateSites
		val frameworkBundles = prj.configurations.findByName(P2_FRAMEWORK_BUNDLES_CONFIG) ?: {
			val config = prj.configurations.create(P2_FRAMEWORK_BUNDLES_CONFIG)
			prj.dependencies.add(P2_FRAMEWORK_BUNDLES_CONFIG, "it.filippor.p2:p2impl:0.0.1")
			config
		}

		p2FrameworkLauncher = createFrameworkLauncher(prj, frameworkBundles)

		stopFrameworkTask = prj.tasks.findByName(P2_STOP_FRAMEWORK_TASK) ?:
			prj.tasks.register(P2_STOP_FRAMEWORK_TASK, TaskWithProgress) [
				doLast([
					if (p2FrameworkLauncher.isStarted) {
						p2FrameworkLauncher.executeWithServiceProvider [
							getService(P2RepositoryManager).tearDown()
						]
						p2FrameworkLauncher.stopFramework()
					}
				])
			].get

		startFrameworkTask = prj.tasks.findByName(P2_START_FRAMEWORK_TASK) ?:
			prj.tasks.register(P2_START_FRAMEWORK_TASK, TaskWithProgress) [ t |
				t.finalizedBy(stopFrameworkTask)
				t.doLast [
					p2FrameworkLauncher.startFramework()
					p2FrameworkLauncher.executeWithServiceProvider [
						getService(P2RepositoryManager).init(
							defaultRepo,
							updateSites,
							new ProgressMonitorWrapper(t)
						)
					]
				]

			].get
	}

	def getDefaultRepo() {
		new DefaultRepo(project.rootProject.buildDir)
	}

	// #{new Artifact("org.eclipse.core.resources", new VersionRange("3.13.300.v20190218-2054"))}
	def p2Bundles(Bundle... bundles) {
		p2Bundles(true, bundles)
	}

	def p2Bundles(boolean transitive, Bundle... bundles) {
		val resolve = project.tasks.register("resolveP2" + Arrays.toString(bundles), ResolveTask) [
			p2FrameworkLauncher = this.p2FrameworkLauncher
			bundles = Arrays.asList(bundles)
			it.transitive = transitive
		]
		resolve.configure[dependsOn += startFrameworkTask]
		stopFrameworkTask.mustRunAfter(stopFrameworkTask.mustRunAfter, resolve)
		return project.files(project.tasks.register(Arrays.toString(bundles), FileProviderTask)[
			resolver = resolve.get
		])
	}

	def TaskProvider<PublishTask> publishTask(String name, Action<PublishTask> action) {
		var publishTask = project.tasks.register(name, PublishTask, action)

		publishTask.configure [
			dependsOn += startFrameworkTask
			p2FrameworkLauncher = FrameworkTaskConfigurator.this.p2FrameworkLauncher
		]
		stopFrameworkTask.mustRunAfter(stopFrameworkTask.mustRunAfter, publishTask)
//		project.tasks.getAt("build").dependsOn += publishTask
		publishTask

	}

	def createFrameworkLauncher(Project project, Iterable<File> bundles) {
		val frameworkStoragePath = project.buildDir.toPath.resolve("tmp").resolve("p2Framework").toFile
		val p2ApiPackage = #{"it.filippor.p2.api"}
		val startBundlesSymbolicNames = #["org.eclipse.equinox.ds", "org.eclipse.equinox.registry",
			"org.eclipse.core.net", "org.apache.felix.scr", "p2impl"]
		return new FrameworkLauncher(frameworkStoragePath, p2ApiPackage, startBundlesSymbolicNames, bundles)
	}

	def doLastOnFramework(Task task, BiConsumer<Task, ServiceProvider> action) {
		task.dependsOn += startFrameworkTask
		stopFrameworkTask.mustRunAfter(stopFrameworkTask.mustRunAfter, task)

		task.doLast [ t |
			if (!p2FrameworkLauncher.isStarted()) {
				t.logger.warn("framework is not running")
				p2FrameworkLauncher.startFramework()
			}

			p2FrameworkLauncher.executeWithServiceProvider [
				action.accept(t, it)
			]
		]
	}
}
