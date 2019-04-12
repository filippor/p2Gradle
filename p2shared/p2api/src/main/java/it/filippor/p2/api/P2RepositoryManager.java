package it.filippor.p2.api;

public interface P2RepositoryManager {

  Object resolve(DefaultRepo repo, String site, String artifactId, String version);

}