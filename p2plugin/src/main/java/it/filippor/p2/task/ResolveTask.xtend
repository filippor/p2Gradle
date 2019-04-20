package it.filippor.p2.task

import it.filippor.p2.api.Bundle
import it.filippor.p2.api.P2RepositoryManager
import it.filippor.p2.framework.FrameworkLauncher
import it.filippor.p2.util.ProgressMonitorWrapper
import java.nio.file.Files
import java.util.Collection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

class ResolveTask  extends TaskWithProgress {
	public var FrameworkLauncher p2FrameworkLauncher
	public Collection<Bundle> bundles
	public boolean transitive
	
	@Input
	def getTransitive() { transitive}

	@Input
	def getBundles() { bundles }
	
	@OutputFile
	def getOutputFile() {
		return project.buildDir.toPath.resolve(name).toFile
	}
	@TaskAction
	def resolve() {
		if (!p2FrameworkLauncher.isStarted()) {
			logger.warn("framework is not running")
			p2FrameworkLauncher.startFramework()
		}
		p2FrameworkLauncher.executeWithServiceProvider [
			val paths = getService(P2RepositoryManager).resolve(
				bundles, 
				transitive, 
				new ProgressMonitorWrapper(this)
			).map [toPath]
			
			logger.info("resolved: {}",paths)
			val outputPath = outputFile.toPath;
			Files.deleteIfExists(outputPath)
			Files.write(outputPath, paths.map[toString])
		]
	}
}