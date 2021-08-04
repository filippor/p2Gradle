/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Java Library project to get you started.
 * For more details take a look at the Java Libraries chapter in the Gradle
 * User Manual available at https://docs.gradle.org/5.3.1/userguide/java_library_plugin.html
 */

plugins {
    // Apply the java-library plugin to add support for Java Library
    id ("java-library")
    id ("it.filippor.p2") version "0.0.9"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}


p2.updateSites = listOf(
                uri("http://download.eclipse.org/releases/2019-12"),
                uri("http://download.eclipse.org/releases/2019-06"))

p2.publishTask("p2publish") {
     repo = uri("$buildDir/targetSite/")
     bundles = configurations.getByName("runtimeClasspath")
   }

dependencies {
  api(p2.bundles(false, "org.eclipse.core.resources:[3.13,3.14)"))
  implementation(project(path = ":p2testNested"))
}
