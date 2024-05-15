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
   id ("biz.aQute.bnd.builder") version("7.0.0")
   id ("it.filippor.p2")
}



p2.setUpdateSites( mutableListOf(
                uri("https://download.eclipse.org/releases/2019-03"),
                uri("https://download.eclipse.org/releases/2019-06")))

p2.publishTask("p2publish") {
     setRepo(layout.buildDirectory.dir("targetSite"))
     setBundles(configurations.getByName("runtimeClasspath"))
}

repositories {
    // You can declare any Maven/Ivy/file repository here.
    mavenCentral()
}



dependencies {
    // This dependency is exported to consumers, that is to say found on their compile classpath.
    implementation("org.apache.commons:commons-math3:3.6.1")
    //implementation(p2.bundles(false, "org.eclipse.core.resources:[3.13,3.14)"))
    api(p2.bundles(true, "org.eclipse.swtbot.eclipse.finder:2.2.1"))
    // This dependency is used internally, and not exposed to consumers on their own compile classpath.
    implementation("com.google.guava:guava:27.0.1-jre")

    // Use JUnit test framework
    testImplementation("junit:junit:4.12")
}
