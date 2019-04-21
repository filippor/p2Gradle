package it.filippor.p2;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Arrays;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;

import it.filippor.p2.config.FrameworkTaskConfigurator;
import it.filippor.p2.task.PublishTask;
import it.filippor.p2.util.Extensions;

public class P2Plugin implements Plugin<Project> {
  @Override
  public void apply(final Project prj) {
    Extensions.repositories(prj, (RepositoryHandler it) -> {
      it.mavenCentral();
      it.ivy( ivy -> {
        ivy.setUrl(URI
          .create("http://www.mirrorservice.org/sites/download.eclipse.org/eclipseMirror/oomph/products/repository/plugins/"));
        ivy.patternLayout(l -> {
          l.artifact("/[artifact]_[revision].[ext]");
        });
      });
    });

    final FrameworkTaskConfigurator taskConfigurator = new FrameworkTaskConfigurator(prj, Paths.get(System
      .getProperty("user.home"), ".gradle", "caches", "p2").toUri(), Arrays
        .asList(URI.create("http://download.eclipse.org/releases/2019-03"),
                URI.create("http://download.eclipse.org/releases/2019-06")));

    Configuration compile = prj.getConfigurations().findByName("compile");
    if (compile == null) {
      compile = prj.getConfigurations().create("compile");
    }

    Configuration api = prj.getConfigurations().findByName("api");
    if (api == null) {
      api = prj.getConfigurations().create("api");
    }

    Configuration test = prj.getConfigurations().findByName("prova123");
    if (test == null) {
      test = prj.getConfigurations().create("prova123");
    }
    final Configuration testF = test;

    Extensions.dependencies(prj, (DependencyHandler it) -> {
      it.add("api", taskConfigurator.p2Bundles(false, "org.eclipse.core.resources:[3.13,3.14)"));
      it.add("compile", taskConfigurator.p2Bundles(true, "org.eclipse.core.resources:[3.13,3.14)"));
      it.add(testF.getName(), taskConfigurator.p2Bundles("org.eclipse.core.filesystem:[1.7,1.8)"));
    });

    taskConfigurator.publishTask("publish", (PublishTask it) -> {
      it.setRepo(prj.getBuildDir().toPath().resolve("targetSite").toUri());
      it.setBundles(testF);
    });
  }
}
