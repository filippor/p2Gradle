package it.filippor.p2;

import java.net.URI;
import java.nio.file.Paths;

import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;

import it.filippor.p2.config.FrameworkTaskConfigurator;

/**
 * @author filippo.rossoni
 * P2 plugin
 */
@NonNullApi
public class P2Plugin implements Plugin<Project> {
 
  /**
   *apply plugin
   */
  @Override
  public void apply(final Project prj) {
    
    RepositoryHandler rh = prj.getRootProject().getRepositories();
    {
      rh.mavenCentral();
      rh.maven(mvn->{
    	 mvn.setUrl(URI.create("https://raw.githubusercontent.com/filippor/p2Gradle/repo/repo")); 
      });
      
    };

    final FrameworkTaskConfigurator taskConfigurator = new FrameworkTaskConfigurator(prj, 
          Paths.get(System.getProperty("user.home"), ".gradle", "caches", "p2").toUri());
    
    prj.getExtensions().add(FrameworkTaskConfigurator.class, "p2", taskConfigurator);
    
//    taskConfigurator.setUpdateSites( Arrays.asList(
//                URI.create("http://download.eclipse.org/releases/2019-03"),
//                URI.create("http://download.eclipse.org/releases/2019-06")));
//    
//    Configuration compile = prj.getConfigurations().findByName("compile");
//    if (compile == null) {
//      compile = prj.getConfigurations().create("compile");
//    }
//
//    Configuration api = prj.getConfigurations().findByName("api");
//    if (api == null) {
//      api = prj.getConfigurations().create("api");
//    }
//
//    Configuration test = prj.getConfigurations().findByName("prova123");
//    if (test == null) {
//      test = prj.getConfigurations().create("prova123");
//    }
//    final Configuration testF = test;
//
//    Extensions.dependencies(prj, (DependencyHandler it) -> {
//      it.add("api", taskConfigurator.p2Bundles(false, "org.eclipse.core.resources:[3.13,3.14)"));
//      it.add("compile", taskConfigurator.p2Bundles(true, "org.eclipse.core.resources:[3.13,3.14)"));
//      it.add(testF.getName(), taskConfigurator.p2Bundles("org.eclipse.core.filesystem:[1.7,1.8)"));
//    });

//    taskConfigurator.publishTask("p2Publish", (PublishTask it) -> {
//      it.setRepo(prj.getBuildDir().toPath().resolve("targetSite").toUri());
//      it.setBundles(testF);
//    });
  }
}
