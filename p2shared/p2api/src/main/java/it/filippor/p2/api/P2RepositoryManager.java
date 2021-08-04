package it.filippor.p2.api;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
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
  Set<File> resolve(Collection<Bundle> artifacts, boolean transitive,Map<String, String> targetProperties, ProgressMonitor monitor);

//  /**
//   * publish artifact in bundleLocations to repository located at repo uri
//   * @param repo repository to publish artifact
//   * @param bundleLocations artifact to publish
//   * @param repositoryProperties properties o repository
//   * @param monitor progress monitor
//   */
//  default void publish(URI repo, Iterable<File> bundleLocations,Map<String, String> repositoryProperties, ProgressMonitor monitor) {
//    publish(bundleLocations, 
//        new RepositoryData(repo, "Metadata Repository", RepositoryData.METADATA_TYPE_SIMPLE_REPOSITORY, repositoryProperties), 
//        new RepositoryData(repo, "Artifact Repository", RepositoryData.ARTIFACT_TYPE_SIMPLE_REPOSITORY, repositoryProperties), 
//        monitor);
//  }
  
  /**
   * publish artifact in bundleLocations to repository 
   * @param bundleLocations bundle to publish
   * @param featureLocations feature to publish
   * @param metadataRepository metadata repository
   * @param artifactRepository artifact repository
   * @param monitor progress monitor
   */
  void publish(Iterable<File> bundleLocations, Iterable<File> featureLocations, RepositoryData metadataRepository,
      RepositoryData artifactRepository, ProgressMonitor monitor);

}