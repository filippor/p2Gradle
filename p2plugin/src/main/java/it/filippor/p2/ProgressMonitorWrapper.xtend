package it.filippor.p2

import it.filippor.p2.api.ProgressMonitor
import it.filippor.p2.task.TaskWithProgress
import org.gradle.internal.logging.progress.ProgressLogger
import org.gradle.internal.logging.progress.ProgressLoggerFactory

class ProgressMonitorWrapper implements ProgressMonitor {
	int totalWork = 0
	int worked = 0
	String taskName
	String subName
	ProgressLoggerFactory progressFactory;

	boolean cancelled

	ProgressLogger progress
	

	new(TaskWithProgress task) {
		progressFactory = task.progressLoggerFactory
	}

	override beginTask(String name, int totalWork) {
		taskName = name
		this.progress = progressFactory.newOperation("aaazzz")

		this.totalWork = totalWork;
		subName = "init"
		progress.start(name,subName)
	}

	override done() {
		progress.completed
	}

	override internalWorked(double work) {
//		throw new UnsupportedOperationException("TODO: auto-generated method stub")
	}

	override isCanceled() {
		return cancelled;
	}

	override setCanceled(boolean value) {
		cancelled = value
	}

	override setTaskName(String name) {
		taskName = name

	}

	override subTask(String name) {
		subName = name
//		progress.start(taskName,subName)
	}

	override worked(int work) {
		this.worked += work
		var msg = '''«taskName»:«subName» «100*worked/totalWork»%'''
			progress.progress(msg)
	}

}
