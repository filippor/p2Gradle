package it.filippor.p2.task

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.internal.logging.progress.ProgressLoggerFactory

class TaskWithProgress extends DefaultTask {
	@Inject
	def ProgressLoggerFactory getProgressLoggerFactory() {
		throw new UnsupportedOperationException()
	}
}
