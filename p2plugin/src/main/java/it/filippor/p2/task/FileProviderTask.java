package it.filippor.p2.task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFiles;


/**
 * @author filippo.rossoni
 * task that read the file created by the resolver task and create a ConfigurableFileCollection
 * used to skip the execution of resolver task if input dosn't change 
 *
 */
public class FileProviderTask extends TaskWithProgress {
  private ResolveTask resolver;

  /**
   * @param resolver task that execute the resolotion
   */
  public void setResolver(final ResolveTask resolver) {
    this.resolver = resolver;
    this.getDependsOn().add(resolver);
  }

  /**
   * @return resolved artifacts
   * @throws IOException if can't read file
   */
  @OutputFiles
  public ConfigurableFileCollection getOutput() throws IOException {
    ConfigurableFileCollection output = this.getProject().files();
    output.builtBy(this);
    if (getInputFile().exists()) {
      output.setFrom(Files.lines(this.getInputFile().toPath()).collect(Collectors.toSet()));
    }
    return output;
  }

  /**
   * @return input files
   */
  @InputFile
  public File getInputFile() {
    return this.resolver.getOutputFile();
  }
}
