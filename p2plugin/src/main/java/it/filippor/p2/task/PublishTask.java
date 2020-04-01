package it.filippor.p2.task;

import java.io.File;
import java.net.URI;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;

import it.filippor.p2.api.P2RepositoryManager;
import it.filippor.p2.framework.FrameworkLauncher;
import it.filippor.p2.framework.ServiceProvider;
import it.filippor.p2.util.ProgressMonitorWrapper;

public class PublishTask extends TaskWithProgress {
	private URI repo;

	private Iterable<File> bundles;

	public FrameworkLauncher p2FrameworkLauncher;

	@Input
	public URI getRepo() {
		return this.repo;
	}

	public void setRepo(final URI repo) {
		this.repo = repo;
	}

	@InputFiles
	public Iterable<File> getBundles() {
		return this.bundles;
	}

	public void setBundles(final Iterable<File> test) {
		this.bundles = test;
	}

	@TaskAction
	public void publish() {
		this.p2FrameworkLauncher.executeWithServiceProvider(sp -> sp.getService(P2RepositoryManager.class).publish(repo,
				bundles, ProgressMonitorWrapper.wrap(this)));
	}
}
