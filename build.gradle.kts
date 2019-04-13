tasks.register("run") {
	group = "p2"
    dependsOn(gradle.includedBuild("p2test").task(":p2testNested:run"))
  //  dependsOn(gradle.includedBuild("p2test").task(":run"))
}
tasks.register("clean") {
  group = "build"
      dependsOn(gradle.includedBuild("p2test").task(":p2testNested:clean"))
      dependsOn(gradle.includedBuild("p2test").task(":clean"))
      dependsOn(gradle.includedBuild("p2shared").task(":p2api:clean"))
      dependsOn(gradle.includedBuild("p2shared").task(":p2impl:clean"))
      dependsOn(gradle.includedBuild("p2plugin").task(":clean"))
}
tasks.register("build") {
  group = "build"
      dependsOn(gradle.includedBuild("p2shared").task(":p2api:build"))
      dependsOn(gradle.includedBuild("p2shared").task(":p2impl:build"))
      dependsOn(gradle.includedBuild("p2plugin").task(":build"))
      dependsOn(gradle.includedBuild("p2test").task(":build"))
}
