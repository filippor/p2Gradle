package it.filippor.p2

import it.filippor.p2.config.FrameworkTaskConfigurator
import java.net.URI
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.osgi.framework.VersionRange
import it.filippor.p2.api.Bundle
import java.io.File

class P2Plugin implements Plugin<Project> {

	
	override void apply(Project prj) {

		prj.repositories.add(prj.repositories.mavenCentral)

		val extension taskConfigurator = new FrameworkTaskConfigurator(prj, #{
			URI.create("http://download.eclipse.org/releases/2019-03"),
			URI.create("http://download.eclipse.org/releases/2019-06")
		})

		if(prj.configurations.findByName("api") === null) prj.configurations.create("api")
		prj.dependencies.add("api", p2Bundles(
			new Bundle("org.eclipse.core.resources", new VersionRange("3.13.300.v20190218-2054"))
		))

		publishTask("publish")[
			dependsOn = prj.configurations.findByName("api")
			repo = prj.buildDir.toPath.resolve("targetSite").toUri
			bundles = #{new File("/home/filippor/git/personal/p2Gradle/p2shared/p2api/build/libs/p2api-0.0.1.jar")}
//			bundles = prj.configurations.findByName("api")
		]
	}

}
