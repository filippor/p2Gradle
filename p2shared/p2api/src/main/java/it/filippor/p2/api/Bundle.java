package it.filippor.p2.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.osgi.framework.VersionRange;

public class Bundle implements Serializable {

  private static final long serialVersionUID = 1396517416983719233L;
  String                    id;
  VersionRange              version;

  public Bundle(String id, VersionRange version) {
    super();
    this.id      = id;
    this.version = version;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public VersionRange getVersion() {
    return version;
  }

  public void setVersion(VersionRange version) {
    this.version = version;
  }

  @Override
  public int hashCode() {
    final int prime  = 31;
    int       result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((version == null) ? 0 : version.hashCode());
    return result;
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
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (version == null) {
      if (other.version != null)
        return false;
    } else if (!version.equals(other.version))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Bundle [id=" + id + ", version=" + version + "]";
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeObject(id);
    if(version != null)
      out.writeObject(version.toString());
    else
      out.writeObject(null);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    id = (String) in.readObject();
    String v = (String) in.readObject();
    if (v != null)
      version = new VersionRange(v);
    else
      version = null;
  }

}
