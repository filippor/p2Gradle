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

tasks.register("publish") {
    group = "build"
//    dependsOn(gradle.includedBuild("p2shared").task(":p2api:publish"))
//    dependsOn(gradle.includedBuild("p2shared").task(":p2impl:publish"))
//    dependsOn(gradle.includedBuild("p2plugin").task(":publish"))
    dependsOn(gradle.includedBuild("p2shared").task(":p2api:publishToMavenLocal"))
    dependsOn(gradle.includedBuild("p2shared").task(":p2impl:publishToMavenLocal"))
    dependsOn(gradle.includedBuild("p2plugin").task(":publishToMavenLocal"))
}

tasks.register("p2publish") {
  group = "build"
      dependsOn(gradle.includedBuild("p2test").task(":p2publish"))
}
tasks.register("model1") {
  group = "build"
      dependsOn(gradle.includedBuild("p2test:p2testNested").task(":model"))
}

tasks.register("dep") {
  group = "build"
      dependsOn(gradle.includedBuild("p2shared").task(":p2impl:dependencies"))
}