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
	
	/** removed **/
	compileOnly("org.eclipse.platform:org.eclipse.osgi:3.13.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.osgi_3.13.0.v20180409-1500.jar"))
	
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.operations:2.5.0")
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.core:2.5.0")
	//implementation(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.core_2.5.0.v20180512-1128.jar"))
	/**imported by ^^^^**/
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.common_3.10.0.v20180412-1130.jar"))
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.repository:2.4.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.repository_2.4.0.v20180512-1128.jar"))
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.engine:2.6.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.engine_2.6.0.v20180409-1209.jar"))
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.publisher:1.5.0"){
		exclude(module = "org.eclipse.osgi")
	}
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.publisher_1.5.0.v20180320-1332.jar"))
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.metadata.repository:1.3.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.metadata.repository_1.3.0.v20180302-1057.jar"))

	implementation("org.eclipse.platform:org.eclipse.equinox.p2.artifact.repository:1.2.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.artifact.repository_1.2.0.v20180413-0846.jar"))
	/**imported by ^^^^**/
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.metadata:2.4.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.metadata_2.4.0.v20180320-1220.jar"))
		
	implementation("org.eclipse.platform:org.eclipse.core.runtime:3.14.0") {
		exclude(module = "org.eclipse.osgi")
	}
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.publisher.eclipse:1.3.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.publisher.eclipse_1.3.0.v20180320-1332.jar"))
	
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.core.runtime_3.14.0.v20180417-0825.jar"))
	/**imported by ^^^^**/
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.app_1.3.500.v20171221-2204.jar"))
	
	runtime("org.apache.felix:org.apache.felix.scr:2.1.16")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.apache.felix.scr_2.0.14.v20180117-1452.jar"))

	runtime("org.eclipse.ecf:org.apache.commons.codec:1.9.0.v20170208-1614")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.apache.commons.codec_1.9.0.v20170208-1614.jar"))
	
	runtime("org.eclipse.ecf:org.apache.commons.logging:1.1.1.v201101211721")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.apache.commons.logging_1.1.1.v201101211721.jar"))
	
	runtime("org.eclipse.ecf:org.apache.httpcomponents.httpclient:4.5.2.v20170210-0925")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.apache.httpcomponents.httpclient_4.5.2.v20170210-0925.jar"))
	
	runtime("org.eclipse.ecf:org.apache.httpcomponents.httpcore:4.4.6.v20170210-0925")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.apache.httpcomponents.httpcore_4.4.6.v20170210-0925.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.core.contenttype:3.7.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.core.contenttype_3.7.0.v20180426-1644.jar"))
	/**imported by ^^^^**/
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.preferences_3.7.100.v20180510-1129.jar"))
	/**imported by ^^^^**/
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.registry_3.8.0.v20180426-1327.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.core.jobs:3.10.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.core.jobs_3.10.0.v20180427-1454.jar"))
	runtime("org.eclipse.platform:org.eclipse.core.net:1.3.200") {
		exclude(module = "org.eclipse.osgi")
	}
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.core.net_1.3.200.v20180515-0858.jar"))
	/**imported by ^^^^**/
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.security_1.2.400.v20171221-2204.jar"))
	
		
	runtime("org.eclipse.ecf:org.eclipse.ecf:3.9.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.ecf_3.9.0.v20180402-2015.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.concurrent:1.1.100")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.concurrent_1.1.100.v20171221-2204.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.ds:1.5.100")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.ds_1.5.100.v20171221-2204.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.frameworkadmin:2.1.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.frameworkadmin_2.1.0.v20180131-0638.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.frameworkadmin.equinox:1.1.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.frameworkadmin.equinox_1.1.0.v20180512-1128.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.launcher:1.5.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.launcher_1.5.0.v20180512-1130.jar"))
	
	implementation("org.eclipse.platform:org.eclipse.equinox.p2.director:2.4.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.director_2.4.0.v20180302-1057.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.director.app:1.1.0"){
		//TODO: exclude test
		//org.ow2.sat4j:org.ow2.sat4j.core:2.3.5
		//exclude(module = "org.ow2.sat4j.core")
		//exclude(module = "org.sat4j.core")
		//exclude(group = "org.ow2.sat4j")
	}
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.director.app_1.1.0.v20180502-1549.jar"))
	/**imported by ^^^^**/
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.sat4j.core_2.3.5.v201308161310.jar"))
	/**imported by ^^^^**/
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.sat4j.pb_2.3.5.v201404071733.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.garbagecollector:1.1.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.garbagecollector_1.1.0.v20180103-0918.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.jarprocessor:1.1.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.jarprocessor_1.1.0.v20180512-1128.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.repository.tools:2.2.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.repository.tools_2.2.0.v20180416-1436.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.touchpoint.eclipse:2.2.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.touchpoint.eclipse_2.2.0.v20180302-1057.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.touchpoint.natives:1.3.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.touchpoint.natives_1.3.0.v20180512-1128.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.transport.ecf:1.2.0"){
		/**missing deps on maven central**/
		//org.eclipse.ecf:org.eclipse.ecf.filetransfer:[4.0.0,)
		//org.eclipse.ecf:org.eclipse.ecf.provider.filetransfer:[3.1.0,)
		exclude(group = "org.eclipse.ecf", module = "org.eclipse.ecf.filetransfer" )
		exclude(group = "org.eclipse.ecf", module = "org.eclipse.ecf.provider.filetransfer")
	}
//	runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.transport.ecf_1.2.0.v20180222-0922.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.p2.updatesite:1.1.0")
//	runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.p2.updatesite_1.1.0.v20180302-1057.jar"))

	runtime("org.eclipse.platform:org.eclipse.equinox.simpleconfigurator:1.3.0")
//	runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.simpleconfigurator_1.3.0.v20180502-1828.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.simpleconfigurator.manipulator:2.1.0")
//	runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.simpleconfigurator.manipulator_2.1.0.v20180103-0918.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.equinox.util:1.1.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.equinox.util_1.1.0.v20180419-0833.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.osgi.compatibility.state:1.1.100"){
		exclude(module = "org.eclipse.osgi")
	}
//	runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.osgi.compatibility.state_1.1.100.v20180331-1743.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.osgi.services:3.7.0")
//	runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.osgi.services_3.7.0.v20180223-1712.jar"))
	
	runtime("org.eclipse.platform:org.eclipse.osgi.util:3.5.0")
//	runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.osgi.util_3.5.0.v20180219-1511.jar"))
	

	runtime("org.eclipse.tycho:org.eclipse.tycho.noopsecurity:1.3.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.tycho.noopsecurity_1.3.0.jar"))

	runtime("org.tukaani:xz:1.8")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.tukaani.xz_1.8.0.v20180207-1613.jar"))
	
	runtime("org.eclipse.ecf:org.eclipse.ecf.identity:3.9.0")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.ecf.identity_3.9.0.v20180426-1936.jar"))
		
	
	
	
	runtime("not_in_maven_central:org.eclipse.ecf.filetransfer:5.0.100.v20180301-0132")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.ecf.filetransfer_5.0.100.v20180301-0132.jar"))
	runtime("not_in_maven_central:org.eclipse.ecf.provider.filetransfer:3.2.400.v20180306-0429")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.ecf.provider.filetransfer_3.2.400.v20180306-0429.jar"))
	runtime("not_in_maven_central:org.eclipse.ecf.provider.filetransfer.httpclient4:1.1.300.v20180301-0132")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.ecf.provider.filetransfer.httpclient4_1.1.300.v20180301-0132.jar"))
	runtime("not_in_maven_central:org.eclipse.ecf.provider.filetransfer.httpclient4.ssl:1.1.100.v20180301-0132")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.ecf.provider.filetransfer.httpclient4.ssl_1.1.100.v20180301-0132.jar"))
	runtime("not_in_maven_central:org.eclipse.ecf.provider.filetransfer.ssl:1.0.100.v20180301-0132")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.ecf.provider.filetransfer.ssl_1.0.100.v20180301-0132.jar"))
	runtime("not_in_maven_central:org.eclipse.ecf.ssl:1.2.100.v20180301-0132")
	//runtime(files("file:///home/filippor/.m2/repository/org/eclipse/tycho/tycho-bundles-external/1.3.0/eclipse/plugins/org.eclipse.ecf.ssl_1.2.100.v20180301-0132.jar"))
	
	
	
	
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