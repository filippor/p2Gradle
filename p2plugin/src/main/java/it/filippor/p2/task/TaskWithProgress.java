package it.filippor.p2.task;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;

public class TaskWithProgress extends DefaultTask {

  @Inject
  public ProgressLoggerFactory getProgressLoggerFactory() {
    throw new UnsupportedOperationException();
  }
}
