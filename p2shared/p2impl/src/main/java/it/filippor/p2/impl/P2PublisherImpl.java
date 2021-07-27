package it.filippor.p2.impl;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.StreamSupport;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.publisher.IPublisherAction;
import org.eclipse.equinox.p2.publisher.IPublisherInfo;
import org.eclipse.equinox.p2.publisher.Publisher;
import org.eclipse.equinox.p2.publisher.PublisherInfo;
import org.eclipse.equinox.p2.publisher.eclipse.BundlesAction;
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

	public void publish(URI repo, Iterable<File> bundleLocations, IProgressMonitor monitor)
			throws ProvisionException, OperationCanceledException {
		if(LOGGER.isLoggable(Level.INFO))
		  if(!bundleLocations.iterator().hasNext()) LOGGER.info("no file to publish");
		  else StreamSupport.stream(bundleLocations.spliterator(), false).forEach(f->LOGGER.info("publishing: " + f));
		  
	    IPublisherInfo info = createPublisherInfo(repo, monitor);
		IPublisherAction[] actions = createActions(bundleLocations);

		Publisher publisher = new Publisher(info);
		publisher.publish(actions, monitor);
		metadataRepositoryManager.removeRepository(info.getMetadataRepository().getLocation());
		artifactRepositoryManager.removeRepository(info.getArtifactRepository().getLocation());
		
		
	}

	private IPublisherInfo createPublisherInfo(URI repo, IProgressMonitor monitor)
			throws ProvisionException, OperationCanceledException {
		PublisherInfo result = new PublisherInfo();

		IMetadataRepository metadataRepository;
		try {
			metadataRepository = metadataRepositoryManager.loadRepository(repo, monitor);
		} catch (ProvisionException e) {
			metadataRepository = metadataRepositoryManager.createRepository(repo, "Metadata Repository",
					IMetadataRepositoryManager.TYPE_SIMPLE_REPOSITORY, Collections.emptyMap());
		}
		IArtifactRepository artifactRepository;
		try {
			artifactRepository = artifactRepositoryManager.loadRepository(repo, monitor);
		} catch (ProvisionException e) {
			artifactRepository = artifactRepositoryManager.createRepository(repo, "Artifact Repository",
					IArtifactRepositoryManager.TYPE_SIMPLE_REPOSITORY, Collections.emptyMap());
		}
		result.setMetadataRepository(metadataRepository);
		result.setArtifactRepository(artifactRepository);
		result.setArtifactOptions(IPublisherInfo.A_PUBLISH | IPublisherInfo.A_INDEX);
		return result;
	}

	private static IPublisherAction[] createActions(Iterable<File> bundleLocations) {
		IPublisherAction[] result = new IPublisherAction[1];
		ArrayList<File> bundles = new ArrayList<>();
		bundleLocations.forEach(bundles::add);
		BundlesAction bundlesAction = new BundlesAction(bundles.toArray(new File[bundles.size()]));
		result[0] = bundlesAction;
		return result;
	}
}
