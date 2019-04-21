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
}
