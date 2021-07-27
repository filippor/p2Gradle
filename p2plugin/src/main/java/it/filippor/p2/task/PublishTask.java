package it.filippor.p2.task;

import java.io.File;
import java.net.URI;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import it.filippor.p2.api.P2RepositoryManager;
import it.filippor.p2.config.FrameworkTaskConfigurator;
import it.filippor.p2.framework.FrameworkLauncher;
import it.filippor.p2.util.ProgressMonitorWrapper;

/**
 * @author filippo.rossoni
 * Task to publish artifact to repository
 */
public class PublishTask extends TaskWithProgress {
	private URI repo;

	private Iterable<File> bundles;

	public PublishTask() {
      repo = getProject().getBuildDir().toPath().resolve("targetSite").toUri();
      bundles = getProject().getConfigurations().getByName("runtimeClasspath");
    }
	
	/**
	 * FrameworkLauncher used to publish bundles
	 */
	public FrameworkLauncher p2FrameworkLauncher;

	/**
	 * @return repository uri
	 */
	@Input
	public URI getRepo() {
		return this.repo;
	}

	/**
	 * URI of the repository in witch the bundles will be published
	 * @param repo uri of the repo
	 */
	public void setRepo(final URI repo) {
		this.repo = repo;
	}

	/**
	 * @return bundles that will be published on repository
	 */
	@InputFiles
	public Iterable<File> getBundles() {
		return this.bundles;
	}

	/**
	 * bundles that will be published on repository
	 * @param bundles artifact to deploy
	 */
	public void setBundles(final Iterable<File> bundles) {
		this.bundles = bundles;
	}

	/**
	 * publish bundle to repo
	 */
	@TaskAction
	public void publish() {
	FrameworkLauncher frameworkLauncher = this.p2FrameworkLauncher;
    if(frameworkLauncher == null)
      frameworkLauncher = getProject().getExtensions().findByType(FrameworkTaskConfigurator.class).getP2FrameworkLauncher();
	
	frameworkLauncher.executeWithServiceProvider(sp -> sp.getService(P2RepositoryManager.class).publish(repo,
				bundles, ProgressMonitorWrapper.wrap(this)));
	}
}
