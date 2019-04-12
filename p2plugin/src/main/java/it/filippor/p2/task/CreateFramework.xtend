package it.filippor.p2.task

import it.filippor.p2.FrameworkLauncher
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class CreateFramework extends DefaultTask {
	var public FrameworkLauncher frameworkLauncher 
	
	var public Iterable<File> bundles = newArrayList
	var public Iterable<File> bundlesToStart = newArrayList

	@OutputDirectory
	def getStoragePath() { frameworkLauncher.frameworkStorage }

	@Input
	def getFrameworkLauncher() { frameworkLauncher }

	@InputFiles
	def getBundles() { bundles }

	@InputFiles
	def getBundlesToStart() { bundlesToStart }

	@TaskAction
	def reCreateFramework() {
		project.rootProject.delete(storagePath)
		frameworkLauncher.createFramework(bundles, bundlesToStart)
	}

}
