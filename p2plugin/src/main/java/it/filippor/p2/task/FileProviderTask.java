package it.filippor.p2.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFiles;

public class FileProviderTask extends TaskWithProgress {
  private ResolveTask resolver;

  public void setResolver(final ResolveTask resolver) {
    this.resolver = resolver;
    this.getDependsOn().add(resolver);
  }

  @OutputFiles
  public ConfigurableFileCollection getOutput() throws IOException {
    ConfigurableFileCollection output = this.getProject().files();
    output.builtBy(this);
    if (getInputFile().exists()) {
      output.setFrom(Files.lines(this.getInputFile().toPath()).collect(Collectors.toSet()));
    }
    return output;
  }

  @InputFile
  public File getInputFile() {
    return this.resolver.getOutputFile();
  }
}
