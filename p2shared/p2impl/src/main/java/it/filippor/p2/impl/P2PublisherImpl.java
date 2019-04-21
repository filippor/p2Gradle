package it.filippor.p2.impl;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.internal.p2.artifact.repository.ArtifactRepositoryManager;
import org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactRepositoryFactory;
import org.eclipse.equinox.internal.p2.metadata.repository.MetadataRepositoryManager;
import org.eclipse.equinox.internal.p2.metadata.repository.SimpleMetadataRepositoryFactory;
import org.eclipse.equinox.p2.publisher.IPublisherAction;
import org.eclipse.equinox.p2.publisher.IPublisherInfo;
import org.eclipse.equinox.p2.publisher.Publisher;
import org.eclipse.equinox.p2.publisher.PublisherInfo;
import org.eclipse.equinox.p2.publisher.eclipse.BundlesAction;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;

public class P2PublisherImpl {

  public void publish(URI repo, Iterable<File> bundleLocations, IProgressMonitor monitor) {
    IPublisherInfo     info      = createPublisherInfo(repo);
    IPublisherAction[] actions   = createActions(bundleLocations);
    Publisher          publisher = new Publisher(info);
    publisher.publish(actions, monitor);
  }

  private static IPublisherInfo createPublisherInfo(URI repo) {
    PublisherInfo result = new PublisherInfo();

    // Create the metadata repository. This will fail if a repository already exists here
    IMetadataRepository metadataRepository = new SimpleMetadataRepositoryFactory()
      .create(repo, "Sample Metadata Repository", MetadataRepositoryManager.TYPE_COMPOSITE_REPOSITORY, Collections.emptyMap());

    // Create the artifact repository. This will fail if a repository already exists here
    IArtifactRepository artifactRepository = new SimpleArtifactRepositoryFactory()
      .create(repo, "Sample Artifact Repository", ArtifactRepositoryManager.TYPE_COMPOSITE_REPOSITORY, Collections.emptyMap());

    result.setMetadataRepository(metadataRepository);
    result.setArtifactRepository(artifactRepository);
    result.setArtifactOptions(IPublisherInfo.A_PUBLISH | IPublisherInfo.A_INDEX);
    return result;
  }

  private static IPublisherAction[] createActions(Iterable<File> bundleLocations) {
    IPublisherAction[] result  = new IPublisherAction[1];
    ArrayList<File>    bundles = new ArrayList<>();
    bundleLocations.forEach(bundles::add);
    BundlesAction bundlesAction = new BundlesAction(bundles.toArray(new File[bundles.size()]));
    result[0] = bundlesAction;
    return result;
  }
}
