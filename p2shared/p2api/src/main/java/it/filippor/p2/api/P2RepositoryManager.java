package it.filippor.p2.api;

import java.net.URI;
import it.filippor.p2.api.Artifact;
import it.filippor.p2.api.DefaultRepo;


public interface P2RepositoryManager {


  public ResolveResult resolve(DefaultRepo repo, Iterable<URI> sites, Iterable<Artifact> artifacts, ProgressMonitor monitor);


}