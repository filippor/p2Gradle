package it.filippor.p2.util

import org.gradle.api.Project
import java.util.function.Consumer
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.Configuration

class Extensions {
	def static repositories(Project project, Consumer<RepositoryHandler> action){
		action.accept(project.repositories)
	}
	def static dependencies(Project project, Consumer<DependencyHandler> action){
		action.accept(project.dependencies)
	}
	
	def static add(DependencyHandler depH,Configuration config,Object dep){
		depH.add(config.name,dep)
	}
	
}
