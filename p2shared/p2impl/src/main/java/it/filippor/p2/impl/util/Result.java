package it.filippor.p2.impl.util;

public class Result<A,B> {
  private A hit;
  private B miss;
  public Result(A hit, B miss) {
    this.hit = hit;
    this.miss = miss;
  }
  public A getHit() {
    return hit;
  }
  public B getMiss() {
    return miss;
  }
  @Override
  public int hashCode() {
    final int prime  = 31;
    int       result = 1;
    result = prime * result + ((hit == null) ? 0 : hit.hashCode());
    result = prime * result + ((miss == null) ? 0 : miss.hashCode());
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
    Result<?,?> other = (Result<?,?>) obj;
    if (hit == null) {
      if (other.hit != null)
        return false;
    } else if (!hit.equals(other.hit))
      return false;
    if (miss == null) {
        return other.miss == null;
    } else return miss.equals(other.miss);
  }
  @Override
  public String toString() {
    return "Result [hit=" + hit + ", miss=" + miss + "]";
  }

}
