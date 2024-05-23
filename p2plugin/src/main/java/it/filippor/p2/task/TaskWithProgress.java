package it.filippor.p2.task;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;

/**
 * Gradle task with Logger
 * @author filippo.rossoni
 */
public class TaskWithProgress extends DefaultTask {

	/**
	 * Constructor
	 */
	public TaskWithProgress() {
		super();
	}
  /**
   * Get Logger factory
   * @return logger factory
   */
  @Inject
  public ProgressLoggerFactory getProgressLoggerFactory() {
    throw new UnsupportedOperationException();
  }
}
