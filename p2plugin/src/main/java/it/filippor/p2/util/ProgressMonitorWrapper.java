package it.filippor.p2.util;

import org.gradle.api.logging.Logger;
import org.gradle.internal.logging.progress.ProgressLogger;
import org.gradle.internal.logging.progress.ProgressLoggerFactory;

import it.filippor.p2.api.ProgressMonitor;
import it.filippor.p2.task.TaskWithProgress;

/**
 * @author filippo.rossoni
 *Wrapper to convert gradle TaskWithProgress to it.filippor.p2.api.ProgressMonitor
 */
public class ProgressMonitorWrapper implements ProgressMonitor {
  private int totalWork = 0;

  private int worked = 0;

  private String taskName;

  private String subName;

  private final ProgressLoggerFactory progressFactory;

  private boolean cancelled;

  private ProgressLogger progress;

  private final Logger log;

  /**
   * @param task task to wrap
   * @return progress monitor
   */
  public static ProgressMonitor wrap(final TaskWithProgress task) {
    return new ProgressMonitorWrapper(task);
  }

  private ProgressMonitorWrapper(final TaskWithProgress task) {
    this.log             = task.getLogger();
    this.progressFactory = task.getProgressLoggerFactory();
  }

  @Override
  public void beginTask(final String name, final int totalWork) {
    this.taskName  = name;
    this.progress  = this.progressFactory.newOperation(name);
    this.totalWork = totalWork;
    this.subName   = "";
    this.progress.start(name, this.subName);
  }

  @Override
  public void done() {
    this.progress.progress("");
    this.progress.completed();
  }

  @Override
  public void internalWorked(final double work) {
  }

  @Override
  public boolean isCanceled() {
    return this.cancelled;
  }

  @Override
  public void setCanceled(final boolean value) {
    this.cancelled = value;
  }

  @Override
  public void setTaskName(final String name) {
    this.taskName = name;
  }

  @Override
  public void subTask(final String name) {
    this.subName = name;
    this.log.debug(name);
    String msg = this.taskName + ":" + this.subName + " " + ((100 * this.worked) / this.totalWork) + "%";
    this.progress.progress(msg);
  }

  @Override
  public void worked(final int work) {
    int _worked = this.worked;
    this.worked = (_worked + work);
    String msg = this.taskName + ":" + this.subName + " " + ((100 * this.worked) / this.totalWork) + "%";
    this.progress.progress(msg);
  }
}
