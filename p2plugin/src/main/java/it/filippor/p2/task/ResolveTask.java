package it.filippor.p2.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
 * @author filippo.rossoni
 * Task that download bundles from p2 repository and create a file named as bundle-resolutions[taskName] containing the list of path
 *
 */
public class ResolveTask extends TaskWithProgress {
  /**
   * FrameworkLauncher used to resolve bundles
   */
  public FrameworkLauncher p2FrameworkLauncher;

  /**
   * resolve transitive dependencies
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
   * @return bundles to resolve
   */
  @Input
  public boolean getTransitive() {
    return this.transitive;
  }

  /**
   * @return resolve transitive dependencies
   */
  @Input
  public Collection<Bundle> getBundles() {
    return this.bundles;
  }

  /**
   * @return the targetProperties
   */
  @Input
  public Map<String, String> getTargetProperties() {
    return targetProperties;
  }

  /**
   * @return file containing dependencies path one every line
   */
  @OutputFile
  public File getOutputFile() {
    return this.getProject().getBuildDir().toPath().resolve("bundle-resolutions").resolve(this.getName()).toFile();
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
          .resolve(bundles, transitive,targetProperties ,ProgressMonitorWrapper.wrap(this))
          .stream()
          .map(File::toPath)
          .map(Path::toString)
          .collect(Collectors.toSet());

        final Path outputPath = this.getOutputFile().toPath();

        Files.deleteIfExists(outputPath);
        Files.write(outputPath, paths);
      } catch (IOException e) {
        throw Extensions.sneakyThrow(e);
      }
    });
  }
}
