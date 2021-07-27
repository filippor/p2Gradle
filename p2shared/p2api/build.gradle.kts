plugins {
	id("java-library")
	`maven-publish`
	id("biz.aQute.bnd.builder")
}
buildscript {
	repositories {
		mavenCentral()
	}
}
java {
	    toolchain {
	        languageVersion.set(JavaLanguageVersion.of(11))
	    }
	}
repositories {
	mavenCentral()
}

dependencyLocking {
	    lockAllConfigurations()
}
dependencies {
	api("org.osgi:osgi.core:8.0.0")
	compileOnly("org.osgi:osgi.annotation:8.0.0")
}

tasks.register<Jar>("sourcesJar") {
	from(sourceSets.main.get().allJava)
	archiveClassifier.set("sources")
}

publishing {
	publications {
		repositories {
	        maven {
	            // change to point to your repo, e.g. http://my.org/repo
	            url = uri(rootProject.buildDir.toPath().getParent().getParent().getParent().resolve("p2GradleRepo").resolve("repo"))
	        }
	    }
	
		create<MavenPublication>("p2api") {
			artifactId = "p2api"
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
				name.set("p2api")
				description.set("Api to resolve and publish p2 bundle")
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