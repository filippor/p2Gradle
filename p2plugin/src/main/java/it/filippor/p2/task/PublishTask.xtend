package it.filippor.p2.task

import it.filippor.p2.api.P2RepositoryManager
import it.filippor.p2.framework.FrameworkLauncher
import it.filippor.p2.util.ProgressMonitorWrapper
import java.io.File
import java.net.URI
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

class PublishTask extends TaskWithProgress {
	URI repo
	Iterable<File> bundles
	public FrameworkLauncher p2FrameworkLauncher

	@Input
	def getRepo() { repo }
	def setRepo(URI repo){this.repo = repo}
	
	@InputFiles
	def getBundles() { bundles }
	def setBundles(Iterable<File> bundles){this.bundles = bundles}
	
	@TaskAction
	def publish() {
		p2FrameworkLauncher.executeWithServiceProvider [ sp |
			var rm = sp.getService(P2RepositoryManager)
			rm.publish(repo, bundles, new ProgressMonitorWrapper(this))
		]
	}

}
