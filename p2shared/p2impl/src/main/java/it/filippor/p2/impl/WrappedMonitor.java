package it.filippor.p2.impl;

import it.filippor.p2.api.ProgressMonitor;

import org.eclipse.core.runtime.IProgressMonitor;

public class WrappedMonitor implements IProgressMonitor {

  private final ProgressMonitor monitor;

  private WrappedMonitor(ProgressMonitor monitor) {
    this.monitor = monitor;
  }

  public static IProgressMonitor wrap(ProgressMonitor monitor) {
    if (monitor == null) {
        return null;
    }
    return new WrappedMonitor(monitor);
  }

  @Override
  public void beginTask(String name, int totalWork) {
    monitor.beginTask(name, totalWork);
  }

  @Override
  public void done() {
    monitor.done();
  }

  @Override
  public void internalWorked(double work) {
    monitor.internalWorked(work);
  }

  @Override
  public boolean isCanceled() {
    return monitor.isCanceled();
  }

  @Override
  public void setCanceled(boolean value) {
    monitor.setCanceled(value);
  }

  @Override
  public void setTaskName(String name) {
    monitor.setTaskName(name);
  }

  @Override
  public void subTask(String name) {
    monitor.subTask(name);
  }

  @Override
  public void worked(int work) {
    monitor.worked(work);
  }

}
