plugins {
    id("java-gradle-plugin")
    id("maven-publish")
//    `kotlin-dsl`
//	kotlin("jvm") version "1.3.21"
//	id("org.xtext.xtend") version("2.0.4")
}

group="it.filippor.p2"
version="0.0.1"

repositories {
    // Use jcenter for resolving your dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
    mavenLocal()
}

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "it.filippor.p2"
            implementationClass = "it.filippor.p2.P2Plugin"
        }
    }
}

dependencies{
	implementation("it.filippor.p2:p2api:0.0.1")
	implementation("org.osgi:osgi.core:7.0.0")
	implementation("org.eclipse.platform:org.eclipse.osgi:3.13.300")
}