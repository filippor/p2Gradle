package it.filippor.p2.api;

import java.io.Serializable;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

public class RepositoryData implements Serializable {
  
  

  @Override
  public String toString() {
    return "RepositoryData [name=" + name + ", uri=" + uri + "]";
  }
  private static final long serialVersionUID = 1198879998098369009L;

  /**
   * Repository type for a simple repository based on a URL or local file system location.
   */
  public static final String METADATA_TYPE_SIMPLE_REPOSITORY = "org.eclipse.equinox.p2.metadata.repository.simpleRepository"; //$NON-NLS-1$

  /**
   * Repository type for a composite repository based on a URL or local file system location.
   */
  public static final String METADATA_TYPE_COMPOSITE_REPOSITORY = "org.eclipse.equinox.p2.metadata.repository.compositeRepository"; //$NON-NLS-1$

  /**
   * Repository type for a simple repository based on a URL or local file system location.
   */
  public static final String ARTIFACT_TYPE_SIMPLE_REPOSITORY = "org.eclipse.equinox.p2.artifact.repository.simpleRepository"; //$NON-NLS-1$

  /**
   * Repository type for a composite repository based on a URL or local file system location.
   */
  public static final String ARTIFACT_TYPE_COMPOSITE_REPOSITORY = "org.eclipse.equinox.p2.artifact.repository.compositeRepository"; //$NON-NLS-1$

  
  private String name;
  private URI uri;
  private String type;
  private Map<String, String> properties;

  public static RepositoryData simpleMetadata(URI uri) {
    return new RepositoryData(uri, "Metadate Repository", METADATA_TYPE_SIMPLE_REPOSITORY, Map.of());
  }
  public static RepositoryData simpleMetadata(URI uri, Map<String, String> properties) {
    return new RepositoryData(uri, "Metadate Repository", METADATA_TYPE_SIMPLE_REPOSITORY, properties);
  }
  public static RepositoryData simpleArtifact(URI uri) {
    return new RepositoryData(uri, "Artifact Repository", ARTIFACT_TYPE_SIMPLE_REPOSITORY, Map.of());
  }
  public static RepositoryData simpleArtifact(URI uri, Map<String, String> properties) {
    return new RepositoryData(uri, "Artifact Repository", ARTIFACT_TYPE_SIMPLE_REPOSITORY, properties);
  }
  
  public RepositoryData(URI uri, String name, String type, Map<String, String> properties) {
    this.uri = uri;
    this.name = name;
    this.type = type;
    this.properties = properties;
  }

  /**
   * @return the uri
   */
  public URI getUri() {
    return uri;
  }

  /**
   * @param uri the uri to set
   * @return this for fluent api
   */
  public RepositoryData setUri(URI uri) {
    this.uri = uri;
    return this;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @param type the type to set
   *  @return this for fluent api
   */
  public RepositoryData setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name the name to set
   *  @return this for fluent api
   */
  public RepositoryData setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * @return the properties
   */
  public Map<String, String> getProperties() {
    return properties;
  }

  /**
   * @param properties the properties to set
   *  @return this for fluent api
   */
  public RepositoryData setProperties(Map<String, String> properties) {
    this.properties = properties;
    return this;
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(name, properties, type, uri);
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    RepositoryData other = (RepositoryData) obj;
    return Objects.equals(name, other.name) && Objects.equals(properties, other.properties)
        && Objects.equals(type, other.type) && Objects.equals(uri, other.uri);
  }
}