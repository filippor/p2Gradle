package it.filippor.p2;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;

import org.gradle.api.NonNullApi;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;

import it.filippor.p2.config.FrameworkTaskConfigurator;


/**
 * Plugin to work with p2 repository
 */
@NonNullApi
public class P2Plugin implements Plugin<Project> {
	/**
	 * default constructor
	 */
	public P2Plugin() {}
  /**
   *apply plugin
   */
  @Override
  public void apply(final Project prj) {
    
    RepositoryHandler rh = prj.getRootProject().getRepositories();
    {
      rh.mavenCentral();
      rh.maven(mvn->{
         mvn.setName("it.filippor.p2.repo");
    	 mvn.setUrl(URI.create("https://raw.githubusercontent.com/filippor/p2Gradle/repo/repo")); 
      });
      
    };

    final FrameworkTaskConfigurator taskConfigurator = new FrameworkTaskConfigurator(prj,
        Paths.get(System.getProperty("user.home"), ".gradle", "caches", "p2", "pool"));

    taskConfigurator.setDefaultTargetProperties(Map.of("org.eclipse.equinox.p2.environments", getOsString()));

    prj.getExtensions().add(FrameworkTaskConfigurator.class, "p2", taskConfigurator);

  }
  
  

  private String getOsString() {
    //TODO: consider other platform
    return "osgi.os=win32,osgi.arch=x86_64,osgi.ws=win32";
  }
}
