package it.filippor.p2.api;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Set;

/**
 * @author filippo.rossoni
 *Repository Manager
 */
public interface P2RepositoryManager {
  
  /**
   * Initialize repository at agent location with remote repository at sites
   * @param agent
   * @param sites
   * @param monitor
   */
  void init(URI agent, Collection<URI> sites, ProgressMonitor monitor);

  /**
   * release resource used by repository manager
   */
  void tearDown();

  
  /**
   * resolve artifacts on remote repository configured in init
   * @param artifacts
   * @param transitive
   * @param monitor
   * @return
   */
  Set<File> resolve(Collection<Bundle> artifacts, boolean transitive, ProgressMonitor monitor);

  /**
   * publish artifact in bundleLocations to repository located at repo uri
   * @param repo
   * @param bundleLocations
   * @param monitor
   */
  void publish(URI repo, Iterable<File> bundleLocations, ProgressMonitor monitor);

}