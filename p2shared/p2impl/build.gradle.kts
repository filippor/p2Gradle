plugins {
	id("java-library")
	`maven-publish`
}
buildscript {
	repositories {
		mavenCentral()
		mavenLocal()
	}
	dependencies {
		classpath("biz.aQute.bnd:biz.aQute.bnd.gradle:4.2.0")
	}
}
apply(plugin = "biz.aQute.bnd.builder")

repositories {
	mavenCentral()
	ivy{
			url=uri("http://www.mirrorservice.org/sites/download.eclipse.org/eclipseMirror/oomph/products/repository/plugins/")
			patternLayout {
	            artifact("/[artifact]_[revision].[ext]")
       		}
	}
}

dependencies {
	// This dependency is exported to consumers, that is to say found on their compile classpath.
	compileOnly(project(":p2api"))
	compileOnly("org.osgi:osgi.annotation:7.0.0")
	compileOnly("org.osgi:osgi.cmpn:7.0.0")
	compileOnly("org.osgi:osgi.core:7.0.0")
	
	compileOnly("org.eclipse.platform:org.eclipse.osgi:3.15.200")
	
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.operations:2.5.700")
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.core:2.6.300")
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.repository:2.4.700")
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.engine:2.6.600")
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.publisher:1.5.300"){
		exclude(module = "org.eclipse.osgi")
	}
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.metadata.repository:1.3.400")

	implementation("org.eclipse.platform:org.eclipse.equinox.p2.artifact.repository:1.3.400")
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.metadata:2.4.700")
		
	implementation("org.eclipse.platform:org.eclipse.core.runtime:3.17.100") {
		exclude(module = "org.eclipse.osgi")
	}
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.publisher.eclipse:1.3.500")
	
	runtime("org.apache.felix:org.apache.felix.scr:2.1.16")
	
	runtime("org.eclipse.platform:org.eclipse.core.contenttype:3.7.600")
	
	runtime("org.eclipse.platform:org.eclipse.core.jobs:3.10.0")
	runtime("org.eclipse.platform:org.eclipse.core.net:1.3.200") {
		exclude(module = "org.eclipse.osgi")
	}
		
	runtime("org.eclipse.ecf:org.eclipse.ecf:3.9.0")
	
	runtime("org.eclipse.platform:org.eclipse.equinox.concurrent:1.1.100")
	
	runtime("org.eclipse.platform:org.eclipse.equinox.ds:1.5.100")
	
	runtime("org.eclipse.platform:org.eclipse.equinox.frameworkadmin:2.1.0")
	
	runtime("org.eclipse.platform:org.eclipse.equinox.frameworkadmin.equinox:1.1.0")
	
	runtime("org.eclipse.platform:org.eclipse.equinox.launcher:1.5.0")
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.director:2.4.0")
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.director.app:1.1.0"){
		//TODO: exclude test
		//org.ow2.sat4j:org.ow2.sat4j.core:2.3.5
		//exclude(module = "org.ow2.sat4j.core")
		//exclude(module = "org.sat4j.core")
		//exclude(group = "org.ow2.sat4j")
	}
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.garbagecollector:1.1.0")
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.jarprocessor:1.1.0")
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.repository.tools:2.2.0")
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.touchpoint.eclipse:2.2.0")
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.touchpoint.natives:1.3.0")
	
//	runtime("org.eclipse.platform:org.eclipse.equinox.p2.transport.ecf:1.2.400"){
//		/**missing deps on maven central**/
//		//org.eclipse.ecf:org.eclipse.ecf.filetransfer:[4.0.0,)
//		//org.eclipse.ecf:org.eclipse.ecf.provider.filetransfer:[3.1.0,)
//		exclude(group = "org.eclipse.ecf", module = "org.eclipse.ecf.filetransfer" )
//		exclude(group = "org.eclipse.ecf", module = "org.eclipse.ecf.provider.filetransfer")
//	}
	
	runtime("com.github.veithen.cosmos.bootstrap:org.eclipse.equinox.p2.transport.ecf:0.3"){
		exclude(module = "org.eclipse.osgi")
		exclude(module = "org.eclipse.ecf")
		exclude(module = "org.eclipse.equinox.p2.repository")
		exclude(module = "org.eclipse.core.net")
		exclude(module = "org.eclipse.equinox.registry")
		exclude(module = "org.eclipse.core.jobs")
		exclude(module = "org.eclipse.equinox.p2.core")
		exclude(module = "org.eclipse.equinox.common")
		exclude(module = "org.eclipse.ecf.identity")
	}
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.updatesite:1.1.0")

	runtime("org.eclipse.platform:org.eclipse.equinox.simpleconfigurator:1.3.0")
	
	runtime("org.eclipse.platform:org.eclipse.equinox.simpleconfigurator.manipulator:2.1.0")
	
	runtime("org.eclipse.platform:org.eclipse.equinox.util:1.1.0")
	
	runtime("org.eclipse.platform:org.eclipse.osgi.compatibility.state:1.1.100"){
		exclude(module = "org.eclipse.osgi")
	}
	
	runtime("org.eclipse.platform:org.eclipse.osgi.services:3.7.0")
	
	runtime("org.eclipse.platform:org.eclipse.osgi.util:3.5.0")

	runtime("org.eclipse.tycho:org.eclipse.tycho.noopsecurity:1.3.0")

	runtime("org.tukaani:xz:1.8")
	
	runtime("org.eclipse.ecf:org.eclipse.ecf.identity:3.9.0")
	
//	runtime("not_in_maven_central:org.eclipse.ecf.filetransfer:5.0.100.v20180301-0132")
//	runtime("not_in_maven_central:org.eclipse.ecf.provider.filetransfer:3.2.400.v20180306-0429")
//	runtime("not_in_maven_central:org.eclipse.ecf.provider.filetransfer.httpclient4:1.1.300.v20180301-0132")
//	runtime("not_in_maven_central:org.eclipse.ecf.provider.filetransfer.httpclient4.ssl:1.1.100.v20180301-0132")
//	runtime("not_in_maven_central:org.eclipse.ecf.provider.filetransfer.ssl:1.0.100.v20180301-0132")
//	runtime("not_in_maven_central:org.eclipse.ecf.ssl:1.2.100.v20180301-0132")
	
	
	
	
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