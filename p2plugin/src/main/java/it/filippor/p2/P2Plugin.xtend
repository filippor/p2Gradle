package it.filippor.p2

import it.filippor.p2.api.Bundle
import it.filippor.p2.config.FrameworkTaskConfigurator
import java.net.URI
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.osgi.framework.VersionRange

class P2Plugin implements Plugin<Project> {

	
	override void apply(Project prj) {

		prj.repositories.add(prj.repositories.mavenCentral)
		
		//ivy{
		//	url=uri("http://www.mirrorservice.org/sites/download.eclipse.org/eclipseMirror/oomph/products/repository/plugins/")
		//	patternLayout {
	    //        artifact("/[artifact]_[revision].[ext]")
       	//	}
		//}
		prj.repositories.add(prj.repositories.ivy[
			url = URI.create("http://www.mirrorservice.org/sites/download.eclipse.org/eclipseMirror/oomph/products/repository/plugins/")
			patternLayout[
				artifact("/[artifact]_[revision].[ext]")
			]
			
		])
	
		val extension taskConfigurator = new FrameworkTaskConfigurator(prj, #{
			URI.create("http://download.eclipse.org/releases/2019-03"),
			URI.create("http://download.eclipse.org/releases/2019-06")
		})

		val compile = prj.configurations.findByName("compile") ?: prj.configurations.create("compile")
		
		prj.dependencies.add(compile.name, p2Bundles(false,
			new Bundle("org.eclipse.core.resources", new VersionRange("3.13.300.v20190218-2054"))
		))
		
//		prj.configurations.findByName("compile")?:prj.configurations.create("compile").extendsFrom( api)
		
		publishTask("publish")[
//			dependsOn = prj.configurations.findByName("test1")
			repo = prj.buildDir.toPath.resolve("targetSite").toUri
//			bundles = #{new File("/home/filippor/git/personal/p2Gradle/p2shared/p2api/build/libs/p2api-0.0.1.jar")}
//			bundles = prj.rootProject.configurations.findByName(FrameworkTaskConfigurator.P2_FRAMEWORK_BUNDLES_CONFIG)
			bundles = prj.configurations.findByName("compile") 
		]
	}

}
