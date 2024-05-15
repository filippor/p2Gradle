package it.filippor.p2.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import it.filippor.p2.api.Bundle;
import it.filippor.p2.api.P2RepositoryManager;
import it.filippor.p2.framework.FrameworkLauncher;
import it.filippor.p2.framework.ServiceProvider;
import it.filippor.p2.util.Extensions;
import it.filippor.p2.util.ProgressMonitorWrapper;

/**
 * Task that download bundles from p2 repository and create a file named as
 * bundle-resolutions[taskName] containing the list of path
 * 
 * @author filippo.rossoni
 */
public class ResolveTask extends TaskWithProgress {
	/**
	 * default constructor
	 */
	public ResolveTask() {
	}

	/**
	 * FrameworkLauncher used to resolve bundles
	 */
	public FrameworkLauncher p2FrameworkLauncher;

	/**
	 * bundles to resolve
	 */
	public Collection<Bundle> bundles;

	/**
	 * resolve transitive dependencies
	 */
	public boolean transitive;

	/**
	 * artifact repository properties used for resolution
	 */
	public Map<String, String> targetProperties;

	/**
	 * getTransitive
	 * 
	 * @return bundles to resolve
	 */
	@Input
	public boolean getTransitive() {
		return this.transitive;
	}

	/**
	 * getBundles
	 * 
	 * @return resolve transitive dependencies
	 */
	@Input
	public Collection<Bundle> getBundles() {
		return this.bundles;
	}

	/**
	 * getTargetProperties
	 * 
	 * @return the targetProperties
	 */
	@Input
	public Map<String, String> getTargetProperties() {
		return targetProperties;
	}

	/**
	 * getOutputFile
	 * 
	 * @return file containing dependencies path one every line
	 */
	@OutputFile
	public RegularFile getOutputFile() {
		Provider<Directory> dir = this.getProject().getLayout().getBuildDirectory().dir("bundle-resolutions");
		return dir.get().file(this.getName());
	}

	/**
	 * resolve bundles using osgi framework
	 */
	@TaskAction
	public void resolve() {
		if ((!this.p2FrameworkLauncher.isStarted())) {
			this.getLogger().warn("framework is not running");
			this.p2FrameworkLauncher.startFramework();
		}
		this.p2FrameworkLauncher.executeWithServiceProvider((ServiceProvider sp) -> {
			try {
				final Set<String> paths = sp.getService(P2RepositoryManager.class)
						.resolve(bundles, transitive, targetProperties, ProgressMonitorWrapper.wrap(this)).stream()
						.map(File::toPath).map(Path::toString).collect(Collectors.toSet());

				final Path outputPath = this.getOutputFile().getAsFile().toPath();

				Files.deleteIfExists(outputPath);
				Files.write(outputPath, paths);
			} catch (IOException e) {
				throw Extensions.sneakyThrow(e);
			}
		});
	}
}
