import org.gradle.api.tasks.compile.JavaCompile

plugins {
    id("java-gradle-plugin")
    `kotlin-dsl`
//	kotlin("jvm") version "1.3.21"
//	id("org.xtext.xtend") version("2.0.4")
}

repositories {
    // Use jcenter for resolving your dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
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
	compile("it.filippor.p2:p2api:0.0.1")
//	compile(kotlin("gradle-plugin", version = "1.3.21"))
	compile ("org.eclipse.xtend:org.eclipse.xtend.lib:2.17.0")
	compile("org.osgi:osgi.core:7.0.0")
	compile("org.eclipse.platform:org.eclipse.osgi:3.13.300")
}