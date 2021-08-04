package it.filippor.p2.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

import org.osgi.framework.VersionRange;


/**
 * @author filippo.rossoni
 *Data Object that represent an osgi bundle
 */
public class Bundle implements Serializable {

  private static final long serialVersionUID = -5642357738474934782L;
  /**
   * bundle id
   */
  private String            id;
  /**
   * bundle version
   */
  private VersionRange      version;

  /**
   * create a bundle with id and version
   * @param id bundle id
   * @param version bundle version
   */
  public Bundle(String id, VersionRange version) {
    super();
    this.id      = id;
    this.version = version;
  }

  /**
   * 
   * @return id of bundle
   */
  public String getId() {
    return id;
  }

  /**
   * @param id id of bundle
   * @return this for fluent api
   */
  public Bundle setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @return version of bundle
   */
  public VersionRange getVersion() {
    return version;
  }

  /**
   * @param version version of bundle
   * @return this for fluent api
   */
  public Bundle setVersion(VersionRange version) {
    this.version = version;
    return this;
  }

  

  @Override
  public int hashCode() {
    return Objects.hash(id, version);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Bundle other = (Bundle) obj;
    return Objects.equals(id, other.id) && Objects.equals(version, other.version);
  }

  @Override
  public String toString() {
    return "Bundle [id=" + id + ", version=" + version + "]";
  }
  
  
  /**
   * Serialization
   * @param out ObjectOutputStream
   * @throws IOException on write error
   */
  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeObject(id);
    if (version != null)
      out.writeObject(version.toString());
    else
      out.writeObject(null);
  }

  /**
   * Serialization 
   * @param in ObjectInputStream
   * @throws IOException on read error
   * @throws ClassNotFoundException error
   */
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    id = (String) in.readObject();
    String v = (String) in.readObject();
    if (v != null)
      version = new VersionRange(v);
    else
      version = null;
  }

}
