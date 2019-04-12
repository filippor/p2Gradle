package it.filippor.p2.api;

import java.io.File;
import java.net.URI;

public class DefaultRepo {

  private File file;

  public DefaultRepo(File file) {
    this.file = file;
  }

  public File getFile() {
    return file;
  }

  public URI getAgentURI() {
    return file.toPath().resolve("p2").resolve("agent").toUri();
  }

}
