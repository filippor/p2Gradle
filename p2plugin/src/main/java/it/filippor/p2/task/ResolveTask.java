package it.filippor.p2.task;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.xtext.xbase.lib.Exceptions;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import it.filippor.p2.api.Bundle;
import it.filippor.p2.api.P2RepositoryManager;
import it.filippor.p2.framework.FrameworkLauncher;
import it.filippor.p2.framework.ServiceProvider;
import it.filippor.p2.util.ProgressMonitorWrapper;

public class ResolveTask extends TaskWithProgress {
  public FrameworkLauncher p2FrameworkLauncher;

  public Collection<Bundle> bundles;

  public boolean transitive;

  @Input
  public boolean getTransitive() {
    return this.transitive;
  }

  @Input
  public Collection<Bundle> getBundles() {
    return this.bundles;
  }

  @OutputFile
  public File getOutputFile() {
    return this.getProject().getBuildDir().toPath().resolve("bundle-resolutions").resolve(this.getName()).toFile();
  }

  @TaskAction
  public void resolve() throws Exception {
    if ((!this.p2FrameworkLauncher.isStarted())) {
      this.getLogger().warn("framework is not running");
      this.p2FrameworkLauncher.startFramework();
    }
    this.p2FrameworkLauncher.executeWithServiceProvider((ServiceProvider it) -> {
      try {
        final Set<String> paths = it.getService(P2RepositoryManager.class)
          .resolve(this.bundles, this.transitive, new ProgressMonitorWrapper(this))
          .stream()
          .map(File::toPath)
          .map(Path::toString)
          .collect(Collectors.toSet());

        final Path outputPath = this.getOutputFile().toPath();

        Files.deleteIfExists(outputPath);
        Files.write(outputPath, paths);
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    });
  }
}
