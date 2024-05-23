package it.filippor.p2.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import it.filippor.p2.api.RepositoryData;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.publisher.IPublisherAction;
import org.eclipse.equinox.p2.publisher.IPublisherInfo;
import org.eclipse.equinox.p2.publisher.Publisher;
import org.eclipse.equinox.p2.publisher.PublisherInfo;
import org.eclipse.equinox.p2.publisher.eclipse.BundlesAction;
import org.eclipse.equinox.p2.publisher.eclipse.FeaturesAction;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;

public class P2PublisherImpl {

  private static final Logger LOGGER = Logger.getLogger(ArtifactRepositoryFacade.class.getCanonicalName());

  private IMetadataRepositoryManager metadataRepositoryManager;
  private IArtifactRepositoryManager artifactRepositoryManager;

  public P2PublisherImpl(IMetadataRepositoryManager metadataRepositoryManager,
      IArtifactRepositoryManager artifactRepositoryManager) {
    super();
    this.metadataRepositoryManager = metadataRepositoryManager;
    this.artifactRepositoryManager = artifactRepositoryManager;
  }

  public P2PublisherImpl(IProvisioningAgent agent) {
    artifactRepositoryManager = agent.getService(IArtifactRepositoryManager.class);
    metadataRepositoryManager = agent.getService(IMetadataRepositoryManager.class);
  }

  public void publish(RepositoryData metadataRepository, RepositoryData artifactRepository, Iterable<File> bundleLocations,
      Iterable<File> featureLocations, IProgressMonitor monitor) throws ProvisionException, IOException {
    if (LOGGER.isLoggable(Level.INFO)) {
        if (!bundleLocations.iterator().hasNext()) {
            LOGGER.info("no file to publish");
        } else {
            StreamSupport.stream(bundleLocations.spliterator(), false).forEach(f -> LOGGER.info("publishing: " + f));
        }
    }

    PublisherInfo info = new PublisherInfo();
    info.setMetadataRepository(loadOrCreateMetadataRepository(metadataRepository, monitor));
    info.setArtifactRepository(loadOrCreateArtifactRepository(artifactRepository, monitor));
    info.setArtifactOptions(IPublisherInfo.A_PUBLISH | IPublisherInfo.A_INDEX |IPublisherInfo.A_OVERWRITE);
    try {
      Publisher publisher = new Publisher(info);

      IPublisherAction[] actions = createActions(bundleLocations,featureLocations);

      publisher.publish(actions, monitor);
    }finally {
      metadataRepositoryManager.removeRepository(info.getMetadataRepository().getLocation());
      artifactRepositoryManager.removeRepository(info.getArtifactRepository().getLocation());
    }
  }

  private IArtifactRepository loadOrCreateArtifactRepository(RepositoryData repo, IProgressMonitor monitor)
      throws ProvisionException, IOException {
    IArtifactRepository artifactRepository;
    try {
      artifactRepository = artifactRepositoryManager.loadRepository(repo.getUri(), monitor);
      String currentType = artifactRepository.getType();
      if (!isArtifactRepoTypeCompatible(repo, artifactRepository)) {
        LOGGER.warning(() -> String.format("artifact repository %s has change type from %s to %s repository deleting repository %s"
            ,repo.getName(), currentType, repo.getType(),repo.getUri()));
        artifactRepositoryManager.removeRepository(artifactRepository.getLocation());
        deleteRepository(artifactRepository.getLocation());
        throw new ProvisionException("type changed");
      }
      if (!artifactRepository.getProperties().equals(repo.getProperties())) {
        for (Entry<String, String> e : repo.getProperties().entrySet()) {
            artifactRepository.setProperty(e.getKey(), e.getValue(), monitor);
        }
      }
    } catch (ProvisionException e) {
      artifactRepository = artifactRepositoryManager.createRepository(repo.getUri(),  repo.getName(), repo.getType(),
          repo.getProperties());
    }
    return artifactRepository;
  }

  private boolean isArtifactRepoTypeCompatible(RepositoryData requestedType, IArtifactRepository currentType) {
    if(IArtifactRepositoryManager.TYPE_COMPOSITE_REPOSITORY.equals(requestedType.getType())
        && "org.eclipse.equinox.internal.p2.artifact.repository.CompositeArtifactRepository".equals(currentType.getType())) {
        return true;
    }
    return currentType.getType().equals(requestedType.getType());
  }

  private void deleteRepository(URI repo) throws IOException {
    Files.walk(Paths.get(repo)).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
  }

  private IMetadataRepository loadOrCreateMetadataRepository(RepositoryData repo, IProgressMonitor monitor)
      throws ProvisionException, IOException {
    IMetadataRepository metadataRepository;
    try {
      metadataRepository = metadataRepositoryManager.loadRepository(repo.getUri(), monitor);
      String currentType = metadataRepository.getType();
      if (!isMetadateRepositoryCompatible(repo, metadataRepository)) {
        LOGGER.warning(() -> String.format("metadata repository %s has change type from %s to %s repository deleting repository %s"
            ,repo.getName(), currentType, repo.getType(),repo.getUri()));
        metadataRepositoryManager.removeRepository(repo.getUri());
        deleteRepository(metadataRepository.getLocation());
        throw new ProvisionException("type changed");
      }
      if (!metadataRepository.getProperties().equals(repo.getProperties())) {
        for (Entry<String, String> e : repo.getProperties().entrySet()) {
            metadataRepository.setProperty(e.getKey(), e.getValue(), monitor);
        }
      }
    } catch (ProvisionException e) {
      metadataRepository = metadataRepositoryManager.createRepository(repo.getUri(), repo.getName(), repo.getType(),
          repo.getProperties());
    }
    return metadataRepository;
  }

  private boolean isMetadateRepositoryCompatible(RepositoryData  requestedType, IMetadataRepository currentType) {
    if(requestedType.getUri().getScheme().equals("file")
        && IMetadataRepositoryManager.TYPE_SIMPLE_REPOSITORY.equals(requestedType.getType())
        && "org.eclipse.equinox.internal.p2.metadata.repository.LocalMetadataRepository".equals(currentType.getType())) {
      return true ;
    }
    return currentType.getType().equals(requestedType.getType());
  }

  private static IPublisherAction[] createActions(Iterable<File> bundleLocations, Iterable<File> featureLocations) {
    IPublisherAction[] result = new IPublisherAction[] {
        new BundlesAction(StreamSupport.stream(bundleLocations.spliterator(), false).toArray(File[]::new)),
        new FeaturesAction(StreamSupport.stream(featureLocations.spliterator(), false).toArray(File[]::new))
    };
    return result;
  }
}
