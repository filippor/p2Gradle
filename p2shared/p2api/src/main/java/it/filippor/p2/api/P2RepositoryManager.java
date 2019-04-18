package it.filippor.p2.api;

import java.net.URI;
import java.util.Collection;

import it.filippor.p2.api.Bundle;
import it.filippor.p2.api.DefaultRepo;


public interface P2RepositoryManager {


  public ResolveResult resolve(DefaultRepo repo, Iterable<URI> sites, Collection<Bundle> artifacts,boolean transitive, boolean offLine ,ProgressMonitor monitor);


}