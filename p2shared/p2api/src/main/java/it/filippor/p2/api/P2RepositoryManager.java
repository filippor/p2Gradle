package it.filippor.p2.api;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Set;

public interface P2RepositoryManager {

  public Set<File> resolve(DefaultRepo repo, Iterable<URI> sites, Collection<Bundle> artifacts, boolean transitive,
                               ProgressMonitor monitor);

}