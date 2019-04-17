package it.filippor.p2

import it.filippor.p2.api.Artifact
import it.filippor.p2.config.FrameworkTaskConfigurator
import java.net.URI
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.osgi.framework.VersionRange

class P2Plugin implements Plugin<Project> {

	override void apply(Project prj) {
		
		prj.repositories.add(prj.repositories.mavenCentral)

		val extension taskConfigurator = new FrameworkTaskConfigurator(prj,
			#{URI.create("http://download.eclipse.org/releases/2019-03")})

		if(prj.configurations.findByName("api")===null)prj.configurations.create("api")
		prj.dependencies.add("api", p2Bundles(
			new Artifact("org.eclipse.core.resources", new VersionRange("3.13.300.v20190218-2054"))
		))

	}

}
