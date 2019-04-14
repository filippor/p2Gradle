package it.filippor.p2.api;

import java.io.File;

public class ResolveResult {
  public Iterable<File> files;
  public Iterable<Artifact> missingArtifact;
  public ResolveResult(Iterable<File> files, Iterable<Artifact> missingArtifact) {
    this.files           = files;
    this.missingArtifact = missingArtifact;
  }
  @Override
  public String toString() {
    return "ResolveResult [files=" + files + ", missingArtifact=" + missingArtifact + "]";
  }
}
