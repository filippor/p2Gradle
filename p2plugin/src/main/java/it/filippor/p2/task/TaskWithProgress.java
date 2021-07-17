package it.filippor.p2.task;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;

/**
 * @author filippo.rossoni
 * Gradle task with Logger
 */
public class TaskWithProgress extends DefaultTask {

  /**
   * Get Logger factory
   * @return logger factory
   */
  @Inject
  public ProgressLoggerFactory getProgressLoggerFactory() {
    throw new UnsupportedOperationException();
  }
}
