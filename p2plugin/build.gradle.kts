
plugins {
	id("com.gradle.plugin-publish") version "0.15.0"
    id("java-gradle-plugin")
    id("maven-publish")
}

group="it.filippor.p2"
version="0.0.11"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
	mavenCentral()
	maven {
        url = uri("https://raw.githubusercontent.com/filippor/p2Gradle/repo/repo")
    }
}

gradlePlugin {
    plugins {
        create("it.filippor.p2") {
            id = "it.filippor.p2"
            implementationClass = "it.filippor.p2.P2Plugin"
        }
    }
}

dependencyLocking {
    lockAllConfigurations()
}


dependencies{
	compileOnly("it.filippor.p2:p2api:0.0.6")
	compileOnly("org.osgi:osgi.annotation:8.0.0")
	implementation("org.eclipse.platform:org.eclipse.osgi:3.19.0")
	
}

tasks.jar {
    dependsOn(configurations.compileClasspath)
    from ({
        configurations.compileClasspath.get().filter{ it.toString().contains("p2api")}.map {
			if (it.isDirectory()) it else zipTree(it)
        }
    })
}

publishing {
 repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../../local-plugin-repository")
        }
    }
}

pluginBundle {
  // These settings are set for the whole plugin bundle
  website = "https://github.com/filippor/p2Gradle"
  vcsUrl = "https://github.com/filippor/p2Gradle"

  // tags and description can be set for the whole bundle here, but can also
  // be set / overridden in the config for specific plugins
  description = "Get dependency and publish artifact to p2 repository"

  // The plugins block can contain multiple plugin entries.
  //
  // The name for each plugin block below (greetingsPlugin, goodbyePlugin)
  // does not affect the plugin configuration, but they need to be unique
  // for each plugin.

  // Plugin config blocks can set the id, displayName, version, description
  // and tags for each plugin.

  // id and displayName are mandatory.
  // If no version is set, the project version will be used.
  // If no tags or description are set, the tags or description from the
  // pluginBundle block will be used, but they must be set in one of the
  // two places.

  (plugins) {

    // first plugin
    "it.filippor.p2" {
      // id is captured from java-gradle-plugin configuration
      displayName = "P2 Gradle Plugin"
      tags = listOf("p2", "osgi", "dependency")
      version = "0.0.11"
    }
  }
}

    