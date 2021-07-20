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
   * @param agent uri of agent location where p2 agent write data
   * @param sites update site considered in resolution
   * @param monitor progress monitor
   */
  void init(URI agent, Collection<URI> sites, ProgressMonitor monitor);

  /**
   * release resource used by repository manager
   */
  void tearDown();

  
  /**
   * resolve artifacts on remote repository configured in init
   * @param artifacts artifact to rresolve
   * @param transitive consider transitive dependency
   * @param monitor progress monitor
   * @return resolved artifact
   */
  Set<File> resolve(Collection<Bundle> artifacts, boolean transitive, ProgressMonitor monitor);

  /**
   * publish artifact in bundleLocations to repository located at repo uri
   * @param repo repository to publish artifact
   * @param bundleLocations artifact to publish
   * @param monitor progress monitor
   */
  void publish(URI repo, Iterable<File> bundleLocations, ProgressMonitor monitor);

}