package it.filippor.p2;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Collections;

import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Extension;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.IvyArtifactRepository;
import org.gradle.api.artifacts.repositories.IvyPatternRepositoryLayout;

import it.filippor.p2.config.FrameworkTaskConfigurator;
import it.filippor.p2.task.PublishTask;
import it.filippor.p2.util.Extensions;

public class P2Plugin implements Plugin<Project> {
  @Override
  public void apply(final Project prj) {
    Extensions.repositories(prj, (RepositoryHandler it) -> {
      it.mavenCentral();
      it.ivy((Action<IvyArtifactRepository>) (IvyArtifactRepository it_1) -> {
        it_1.setUrl(URI
          .create("http://www.mirrorservice.org/sites/download.eclipse.org/eclipseMirror/oomph/products/repository/plugins/"));
        it_1.patternLayout((Action<IvyPatternRepositoryLayout>) (IvyPatternRepositoryLayout it_2) -> {
          it_2.artifact("/[artifact]_[revision].[ext]");
        });
      });
    });

    @Extension
    final FrameworkTaskConfigurator taskConfigurator = new FrameworkTaskConfigurator(prj, Paths.get(System
      .getProperty("user.home"), ".gradle", "caches", "p2").toUri(), Collections
        .<URI> unmodifiableSet(CollectionLiterals.<URI> newHashSet(URI.create("http://download.eclipse.org/releases/2019-03"),
                                                                   URI.create("http://download.eclipse.org/releases/2019-06"))));

    Configuration _elvis      = null;
    Configuration _findByName = prj.getConfigurations().findByName("compile");
    if (_findByName != null) {
      _elvis = _findByName;
    } else {
      _elvis = prj.getConfigurations().create("compile");
    }
    final Configuration compile = _elvis;

    Configuration _elvis_1      = null;
    Configuration _findByName_1 = prj.getConfigurations().findByName("api");
    if (_findByName_1 != null) {
      _elvis_1 = _findByName_1;
    } else {
      _elvis_1 = prj.getConfigurations().create("api");
    }
    final Configuration api = _elvis_1;

    Configuration _elvis_2      = null;
    Configuration _findByName_2 = prj.getConfigurations().findByName("prova123");
    if (_findByName_2 != null) {
      _elvis_2 = _findByName_2;
    } else {
      Configuration _create_4 = prj.getConfigurations().create("prova123");
      _elvis_2 = _create_4;
    }
    final Configuration test = _elvis_2;

    Extensions.dependencies(prj, (DependencyHandler it) -> {
      Extensions.add(it, api, taskConfigurator.p2Bundles(false, "org.eclipse.core.resources:3.13.300.v20190218-2054"));
      Extensions.add(it, compile, taskConfigurator.p2Bundles(true, "org.eclipse.core.resources:3.13.300.v20190218-2054"));
      Extensions.add(it, test, taskConfigurator.p2Bundles("org.eclipse.core.filesystem:1.7.300.v20190218-2054"));
    });
    
    taskConfigurator.publishTask("publish", (PublishTask it) -> {
      it.setRepo(prj.getBuildDir().toPath().resolve("targetSite").toUri());
      it.setBundles( test);
    });
  }
}
