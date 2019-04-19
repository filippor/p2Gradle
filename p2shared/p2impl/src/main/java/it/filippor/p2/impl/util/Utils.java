package it.filippor.p2.impl.util;

import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.osgi.framework.VersionRange;

import it.filippor.p2.api.Bundle;

public class Utils {
  @SuppressWarnings("unchecked")
  public static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
    throw (E) e;
  }
  public static Iterable<Bundle> toBundles(Set<IInstallableUnit> ius) {
    return ius.stream().map(Utils::toBundle).collect(Collectors.toSet());
  }

  public static Bundle toBundle(IInstallableUnit iu) {
    return new Bundle(iu.getId(), new VersionRange(iu.getVersion().getOriginal()));
  }
}
