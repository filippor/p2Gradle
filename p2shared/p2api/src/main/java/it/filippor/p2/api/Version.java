package it.filippor.p2.api;


public class Version {
  public int major;
  public int minor;
  public int micro;
  public Comparable<? extends Object> qualifier;

  public Version(int major, int minor, int micro, Comparable<? extends Object> qualifier) {
    this.major = major;
    this.minor = minor;
    this.micro = micro;
    //intern the qualifier string to avoid duplication
    if (qualifier instanceof String)
        qualifier = ((String) qualifier).intern();
    this.qualifier = qualifier;
}
}
