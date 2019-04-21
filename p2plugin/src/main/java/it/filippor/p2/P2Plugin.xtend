package it.filippor.p2

import it.filippor.p2.config.FrameworkTaskConfigurator
import java.net.URI
import java.nio.file.Path
import org.gradle.api.Plugin
import org.gradle.api.Project

import static extension it.filippor.p2.util.Extensions.*

class P2Plugin implements Plugin<Project> {

	override void apply(Project prj) {

		prj.repositories [
			mavenCentral()
			ivy[
				url = URI.create(
					"http://www.mirrorservice.org/sites/download.eclipse.org/eclipseMirror/oomph/products/repository/plugins/")
				patternLayout[
					artifact("/[artifact]_[revision].[ext]")
				]
			]
		]

		val extension taskConfigurator = new FrameworkTaskConfigurator(prj,
			Path.of(System.getProperty("user.home"), ".gradle", "caches", "p2").toUri
			, #{
				URI.create("http://download.eclipse.org/releases/2019-03"),
				URI.create("http://download.eclipse.org/releases/2019-06")
			})

		val compile = prj.configurations.findByName("compile") ?: prj.configurations.create("compile")
		val api = prj.configurations.findByName("api") ?: prj.configurations.create("api")
		val test = prj.configurations.findByName("prova123") ?: prj.configurations.create("prova123")

		prj.dependencies [
			add(api, p2Bundles(false, "org.eclipse.core.resources:3.13.300.v20190218-2054"))
			add(compile, p2Bundles(true, "org.eclipse.core.resources:3.13.300.v20190218-2054"))
			add(test, p2Bundles("org.eclipse.core.filesystem:1.7.300.v20190218-2054"))
		]

		publishTask("publish") [
			repo = prj.buildDir.toPath.resolve("targetSite").toUri
//			bundles = #{new File("/home/filippor/git/personal/p2Gradle/p2shared/p2api/build/libs/p2api-0.0.1.jar")}
//			bundles = prj.rootProject.configurations.findByName(FrameworkTaskConfigurator.P2_FRAMEWORK_BUNDLES_CONFIG)
			bundles = test
		]
	}

}
