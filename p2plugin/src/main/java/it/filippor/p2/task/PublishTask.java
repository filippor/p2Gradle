package it.filippor.p2.task;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Objects;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import it.filippor.p2.api.P2RepositoryManager;
import it.filippor.p2.api.RepositoryData;
import it.filippor.p2.config.FrameworkTaskConfigurator;
import it.filippor.p2.framework.FrameworkLauncher;
import it.filippor.p2.util.ProgressMonitorWrapper;

/**
 * @author filippo.rossoni Task to publish artifact to repository
 */
public class PublishTask extends TaskWithProgress {

  private Iterable<File> bundles = Collections.emptyList();
  private Iterable<File> features = Collections.emptyList();
  private RepositoryData metadataRepository;
  private RepositoryData artifactRepository;

  /**
   * default constructor
   */
  public PublishTask() {
    URI repo = getProject().getBuildDir().toPath().resolve("targetSite").toUri();
    bundles = getProject().getConfigurations().getByName("runtimeClasspath");
    metadataRepository = RepositoryData.simpleMetadata(repo);
    artifactRepository = RepositoryData.simpleArtifact(repo);
  }

  /**
   * FrameworkLauncher used to publish bundles
   */
  public FrameworkLauncher p2FrameworkLauncher;
  
  @Internal
  public URI getRepo() {
    if(Objects.equals(metadataRepository.getUri(), artifactRepository.getUri()))
      return metadataRepository.getUri();
    return null;
  }
  
  /**
   * URI of the repository in witch the bundles will be published set both artifact and metadata repository uri
   * 
   * @param repo uri of the repo
   */
  public void setRepo(final URI repo) {
    metadataRepository.setUri(repo);
    artifactRepository.setUri(repo);
  }

  /**
   * @return the metadataRepository
   */
  @Input
  public RepositoryData getMetadataRepository() {
    return metadataRepository;
  }

  /**
   * @param metadataRepository the metadataRepository to set
   */
  public void setMetadataRepository(RepositoryData metadataRepository) {
    this.metadataRepository = metadataRepository;
  }

  /**
   * @return the artifactRepository
   */
  @Input
  public RepositoryData getArtifactRepository() {
    return artifactRepository;
  }

  /**
   * @param artifactRepository the artifactRepository to set
   */
  public void setArtifactRepository(RepositoryData artifactRepository) {
    this.artifactRepository = artifactRepository;
  }

  /**
   * @return bundles that will be published on repository
   */
  @InputFiles
  public Iterable<File> getBundles() {
    return this.bundles;
  }
  /**
   * @return features that will be published on repository
   */
  @InputFiles
  public Iterable<File> getFeatures() {
    return this.features;
  }

  /**
   * bundles that will be published on repository
   * 
   * @param bundles artifact to deploy
   */
  public void setBundles(final Iterable<File> bundles) {
    this.bundles = bundles;
  }
  /**
   * features that will be published on repository
   * 
   * @param features features to deploy
   */
  public void setFeatures(final Iterable<File> features) {
    this.features = features;
  }


  /**
   * publish bundle to repo
   */
  @TaskAction
  public void publish() {
    FrameworkLauncher frameworkLauncher = this.p2FrameworkLauncher;
    if (frameworkLauncher == null)
      frameworkLauncher = getProject().getExtensions().findByType(FrameworkTaskConfigurator.class)
          .getP2FrameworkLauncher();
    frameworkLauncher.executeWithServiceProvider(sp -> {

      sp.getService(P2RepositoryManager.class).publish(bundles,features ,metadataRepository, artifactRepository,
          ProgressMonitorWrapper.wrap(this));
    });

  }
}
