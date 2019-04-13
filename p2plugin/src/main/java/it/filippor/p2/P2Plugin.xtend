package it.filippor.p2

import it.filippor.p2.api.DefaultRepo
import it.filippor.p2.api.P2RepositoryManager
import it.filippor.p2.task.CreateFramework
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

class P2Plugin implements Plugin<Project> {
	val public static P2_FRAMEWORK_BUNDLES_CONFIG = "p2frameworkBundles"

	override void apply(Project prj) {

		val frameworkBundles = prj.configurations.create(P2_FRAMEWORK_BUNDLES_CONFIG)
		prj.dependencies.add(P2_FRAMEWORK_BUNDLES_CONFIG, "org.apache.felix:org.apache.felix.scr:2.1.16")
		prj.dependencies.add(P2_FRAMEWORK_BUNDLES_CONFIG, "it.filippor.p2:p2impl:0.0.1")

		val frameworkStoragePath = prj.rootProject.buildDir.toPath.resolve("tmp").resolve("p2Framework").toFile
		val p2ApiPackage = #{"it.filippor.p2.api"}
		val p2FrameworkLauncher = new FrameworkLauncher(frameworkStoragePath, p2ApiPackage)


		var tasks = prj.rootProject.tasks
		val createFrameworkTask = if (tasks.exists[name.equals("createFramework")])
				tasks.getAt("createFramework")
			else
				tasks.register("createFramework", CreateFramework) [
					frameworkLauncher = p2FrameworkLauncher
					bundles = frameworkBundles
					bundlesToStart = frameworkBundles.explictDeclaredDependencies
				]

		prj.getTasks().register("run") [ t |
			t.group = "p2"
			t.dependsOn += createFrameworkTask
			t.doLast [
				p2FrameworkLauncher.executeWithService(frameworkBundles,P2RepositoryManager) [
					val Object result = resolve(new DefaultRepo(prj.buildDir),
						"http://download.eclipse.org/releases/2019-03", "org.eclipse.core.resources",
						"3.13.300.v20190218-2054")
					t.logger.error("" + result)
				]
			]
		]

	}

	def getExplictDeclaredDependencies(Configuration p2FrameworkBundle) {
		p2FrameworkBundle.copy().transitive = false
	}
}
