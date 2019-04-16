package it.filippor.p2

import it.filippor.p2.api.Artifact
import it.filippor.p2.api.DefaultRepo
import it.filippor.p2.api.P2RepositoryManager
import it.filippor.p2.config.FrameworkTaskConfigurator
import it.filippor.p2.task.TaskWithProgress
import it.filippor.p2.util.ProgressMonitorWrapper
import java.net.URI
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.osgi.framework.VersionRange

class P2Plugin implements Plugin<Project> {

	override void apply(Project prj) {

		val extension taskConfigurator = new FrameworkTaskConfigurator(prj)

		prj.getTasks().register("run", TaskWithProgress) [
			group = "p2"
			val monitor = new ProgressMonitorWrapper(it)
			doLastOnFramework[ it,serviceProvider|
				var repoManager = serviceProvider.getService(P2RepositoryManager)
				val result = repoManager.resolve(
					new DefaultRepo(prj.buildDir),
					#{URI.create("http://download.eclipse.org/releases/2019-03")},
					// "v20190218-2054"
					#{new Artifact("org.eclipse.core.resources",new VersionRange("3.13.300.v20190218-2054"))},
					monitor
				)
				logger.error("" + result)

			]
		]

	}

	def getExplictDeclaredDependencies(Configuration p2FrameworkBundle) {
		p2FrameworkBundle.copy().transitive = false
	}
}
