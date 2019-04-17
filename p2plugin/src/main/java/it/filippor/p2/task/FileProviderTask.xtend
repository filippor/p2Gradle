package it.filippor.p2.task

import it.filippor.p2.framework.FrameworkLauncher
import it.filippor.p2.framework.ServiceProvider
import java.io.File
import java.util.function.BiFunction
import org.gradle.api.tasks.OutputFiles

class FileProviderTask extends TaskWithProgress {

	public var FrameworkLauncher p2FrameworkLauncher

	public var BiFunction<FileProviderTask, ServiceProvider, Iterable<File>> outputFileProvider

	Iterable<File> output

	@OutputFiles
	def Iterable<File> getOutput() {
		if (output === null) {
			if (!p2FrameworkLauncher.isStarted()) {
				FileProviderTask.this.logger.warn("framework is not running")
				p2FrameworkLauncher.startFramework()
			}
			p2FrameworkLauncher.executeWithServiceProvider [
				output = outputFileProvider.apply(FileProviderTask.this, it)
			]
		}
		return output
	}
}
