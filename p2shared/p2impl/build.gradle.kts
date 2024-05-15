plugins {
	id("java-library")
	`maven-publish`
	id("biz.aQute.bnd.builder")
}

repositories {
	mavenCentral()
}

java {
    toolchain {
       languageVersion.set(JavaLanguageVersion.of(17))
    }
}


dependencyLocking {
	    lockAllConfigurations()
}


dependencies {
	// This dependency is exported to consumers, that is to say found on their compile classpath.
	compileOnly(project(":p2api"))
	compileOnly("org.osgi:osgi.core:8.0.0")
	compileOnly("org.osgi:osgi.annotation:8.1.0")
	compileOnly("org.osgi:osgi.cmpn:7.0.0")
	compileOnly("org.eclipse.platform:org.eclipse.osgi:3.19.0")
	
    implementation("org.eclipse.platform:org.eclipse.equinox.common:+")
    implementation("org.eclipse.platform:org.eclipse.equinox.p2.director:+")
    implementation("org.eclipse.platform:org.eclipse.equinox.p2.engine:+")
    implementation("org.eclipse.platform:org.eclipse.equinox.p2.metadata.repository:+")
    implementation("org.eclipse.platform:org.eclipse.equinox.p2.operations:+")
    implementation("org.eclipse.platform:org.eclipse.equinox.p2.publisher:+"){exclude(module = "org.eclipse.osgi")}
    implementation("org.eclipse.platform:org.eclipse.equinox.p2.publisher.eclipse:+")
    implementation("org.eclipse.platform:org.eclipse.equinox.p2.repository:+")

    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.registry:+")
    runtimeOnly("org.eclipse.ecf:org.apache.commons.codec:+")
    runtimeOnly("org.eclipse.ecf:org.apache.commons.logging:+")
    runtimeOnly("org.eclipse.ecf:org.apache.httpcomponents.httpclient:+")
    runtimeOnly("org.eclipse.ecf:org.apache.httpcomponents.httpcore:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.core.net:+"){exclude(module = "org.eclipse.osgi")}
    runtimeOnly("org.eclipse.platform:org.eclipse.core.runtime:+") {exclude(module = "org.eclipse.osgi")}
    runtimeOnly("org.eclipse.ecf:org.eclipse.ecf.identity:+")
    runtimeOnly("org.eclipse.ecf:org.eclipse.ecf.provider.filetransfer.httpclient45:+")
    runtimeOnly("org.eclipse.ecf:org.eclipse.ecf.provider.filetransfer.ssl:+")
    runtimeOnly("org.eclipse.ecf:org.eclipse.ecf.ssl:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.concurrent:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.frameworkadmin:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.frameworkadmin.equinox:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.launcher:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.p2.director.app:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.p2.garbagecollector:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.p2.jarprocessor:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.p2.artifact.repository:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.p2.touchpoint.eclipse:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.p2.touchpoint.natives:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.p2.transport.ecf:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.simpleconfigurator:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.simpleconfigurator.manipulator:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.osgi.compatibility.state:+"){exclude(module = "org.eclipse.osgi")}
    runtimeOnly("org.eclipse.platform:org.eclipse.osgi.services:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.osgi.util:+")
    runtimeOnly("org.eclipse.tycho:org.eclipse.tycho.noopsecurity:+")
    runtimeOnly("org.eclipse.platform:org.eclipse.equinox.ds:+")

	runtimeOnly("org.bouncycastle:bcpg-jdk15on:1.65")
	runtimeOnly("org.bouncycastle:bcprov-jdk15on:1.65.01")
}

tasks.register<Jar>("sourcesJar") {
	from(sourceSets.main.get().allJava)
	archiveClassifier.set("sources")
}

publishing {
	repositories {
	        maven {
	            url = uri(rootProject.buildDir.toPath().getParent().getParent().getParent().resolve("p2GradleRepo").resolve("repo"))
	        }
	    }

	publications {
		create<MavenPublication>("P2impl") {
			artifactId = "p2impl"
			from(components["java"])
			artifact(tasks["sourcesJar"])
			versionMapping {
				usage("java-api") {
					fromResolutionOf("runtimeClasspath")
				}
				usage("java-runtime") {
					fromResolutionResult()
				}
			}
			pom {
				name.set("p2impl")
				description.set("Implementation of p2api")
				url.set("https://github.com/filippor/p2Gradle/")
				licenses {
					license {
						name.set("The Apache License, Version 2.0")
						url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
					}
				}
				developers {
					developer {
						id.set("filippor")
						name.set("Filippo Rosoni")
						email.set("filippo.rossoni@gmail.com")
					}
				}
				scm {
					//connection.set("scm:git:git://example.com/my-library.git")
					//developerConnection.set("scm:git:ssh://example.com/my-library.git")
					url.set("https://github.com/filippor/p2Gradle/")
				}
			}
		}
	}
}