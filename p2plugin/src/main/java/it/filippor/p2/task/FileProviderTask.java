package it.filippor.p2.task;

import it.filippor.p2.task.ResolveTask;
import it.filippor.p2.task.TaskWithProgress;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFiles;

public class FileProviderTask extends TaskWithProgress {
  private ResolveTask resolver;

  public void setResolver(final ResolveTask resolver) {
    this.resolver = resolver;
    this.getDependsOn().add(resolver);
  }

  private ConfigurableFileCollection output;

  @OutputFiles
  public ConfigurableFileCollection getOutput() throws IOException {
      this.output = this.getProject().files();
      this.output.builtBy(this);
      if (this.getInputFile().exists()) {
        this.output.setFrom(Files.lines(this.getInputFile().toPath()).collect(Collectors.toSet()));
      }
      return this.output;
  }

  @InputFile
  public File getInputFile() {
    return this.resolver.getOutputFile();
  }
}
