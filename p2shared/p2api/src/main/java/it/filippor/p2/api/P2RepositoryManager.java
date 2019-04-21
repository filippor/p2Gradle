package it.filippor.p2.api;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Set;

public interface P2RepositoryManager {
  public void init(URI agent, Collection<URI> sites, ProgressMonitor monitor);

  public void tearDown();

  public Set<File> resolve(Collection<Bundle> artifacts, boolean transitive, ProgressMonitor monitor);

  void publish(URI repo, File[] bundleLocations, ProgressMonitor monitor);

}