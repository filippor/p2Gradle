package it.filippor.p2.api;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Set;

public interface P2RepositoryManager {
  void init(URI agent, Collection<URI> sites, ProgressMonitor monitor);

  void tearDown();

  Set<File> resolve(Collection<Bundle> artifacts, boolean transitive, ProgressMonitor monitor);

  void publish(URI repo, Iterable<File> bundleLocations, ProgressMonitor monitor);

}