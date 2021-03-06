plugins {
	id("java-library")
	`maven-publish`
	

}
buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("biz.aQute.bnd:biz.aQute.bnd.gradle:5.0.1")
	}
}
apply(plugin = "biz.aQute.bnd.builder")





repositories {
	mavenCentral()
}



dependencies {
	api("org.osgi:osgi.core:7.0.0")
	compileOnly("org.osgi:osgi.annotation:7.0.0")
}

tasks.register<Jar>("sourcesJar") {
	from(sourceSets.main.get().allJava)
	archiveClassifier.set("sources")
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
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
				description.set("A concise description of my library")
				url.set("http://www.example.com/library")
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
				//scm {
				//	connection.set("scm:git:git://example.com/my-library.git")
				//	developerConnection.set("scm:git:ssh://example.com/my-library.git")
				//	url.set("http://example.com/my-library/")
				//}
			}
		}
	}
}