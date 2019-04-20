package it.filippor.p2.task

import java.io.File
import java.nio.file.Files
import java.util.stream.Collectors
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFiles

class FileProviderTask extends TaskWithProgress {

	ResolveTask resolver

	def setResolver(ResolveTask resolver) {
		this.resolver = resolver
		dependsOn += resolver
	}

	ConfigurableFileCollection output

	@OutputFiles
	def ConfigurableFileCollection getOutput() {

		output = project.files()
		output.builtBy(this)
		if (inputFile.exists) {
			output.from = Files.lines(inputFile.toPath).collect(Collectors.toSet)
		}
		output
	}

	@InputFile
	def File getInputFile() {
		resolver.outputFile
	}

}
