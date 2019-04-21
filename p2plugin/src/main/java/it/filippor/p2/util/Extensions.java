package it.filippor.p2.util;

import java.util.function.Consumer;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;

public class Extensions {
  public static void repositories(final Project project, final Consumer<RepositoryHandler> action) {
    action.accept(project.getRepositories());
  }

  public static void dependencies(final Project project, final Consumer<DependencyHandler> action) {
    action.accept(project.getDependencies());
  }

  public static Dependency add(final DependencyHandler depH, final Configuration config, final Object dep) {
    return depH.add(config.getName(), dep);
  }

  /*
   * shamelessly taken from Project Lombok
   * https://github.com/rzwitserloot/lombok/blob/master/src/core/lombok/Lombok.java
   */
  /**
   * Throws the given exception and sneaks it through any compiler checks. This allows to throw checked exceptions
   * without the need to declare it. Clients should use the following idiom to trick static analysis and dead code
   * checks:
   * 
   * <pre>
   * throw sneakyThrow(new CheckedException("Catch me if you can ;-)")).
   * </pre>
   * 
   * This method is heavily inspired by project
   * <a href="https://github.com/rzwitserloot/lombok/blob/master/src/core/lombok/Lombok.java">Lombok</a>.
   * 
   * @param t
   *          the throwable that should be sneaked through compiler checks. May not be <code>null</code>.
   * @return never returns anything since {@code t} is always thrown.
   * @throws NullPointerException
   *           if {@code t} is <code>null</code>.
   */
  public static RuntimeException sneakyThrow(Throwable t) {
    if (t == null)
      throw new NullPointerException("t");
    sneakyThrow0(t);
    return null;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Throwable> void sneakyThrow0(Throwable t) throws T {
    throw (T) t;
  }
}
