package it.filippor.p2.api;

import java.io.File;

public class ResolveResult {
  public Iterable<File> files;
  public Iterable<Bundle> missingArtifact;
  public ResolveResult(Iterable<File> files, Iterable<Bundle> missingArtifact) {
    this.files           = files;
    this.missingArtifact = missingArtifact;
  }
  @Override
  public String toString() {
    return "ResolveResult [files=" + files + ", missingArtifact=" + missingArtifact + "]";
  }
}
