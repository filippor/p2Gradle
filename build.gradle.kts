

tasks.register("clean") {
  group = "build"
      dependsOn(gradle.includedBuild("p2test").task(":p2testNested:clean"))
      dependsOn(gradle.includedBuild("p2test").task(":clean"))
      dependsOn(gradle.includedBuild("testGroovy").task(":clean"))
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
    dependsOn(gradle.includedBuild("p2shared").task(":p2api:publishP2apiPublicationToMavenRepository"))
    dependsOn(gradle.includedBuild("p2shared").task(":p2impl:publishP2implPublicationToMavenRepository"))
    dependsOn(gradle.includedBuild("p2plugin").task(":publishToMavenLocal"))
}

tasks.register("hello") {
  group = "hello"
      dependsOn(gradle.includedBuild("frameworkTest").task(":hello"))
}
tasks.register("p2publish") {
  group = "build"
      dependsOn(gradle.includedBuild("p2test").task(":p2publish"))
}
tasks.register("modelTest") {
  group = "build"
      dependsOn(gradle.includedBuild("p2test").task(":p2testNested:model"))
}
tasks.register("modelApi") {
  group = "build"
      dependsOn(gradle.includedBuild("p2shared").task(":p2api:model"))
}
tasks.register("modelPlugin") {
  group = "build"
      dependsOn(gradle.includedBuild("p2plugin").task(":model"))
}
tasks.register("publishPlugin") {
  group = "build"
      dependsOn(gradle.includedBuild("p2plugin").task(":publishPlugins"))
}
tasks.register("publishPluginLocal") {
  group = "build"
      dependsOn(gradle.includedBuild("p2plugin").task(":publish"))
}

tasks.register("depP2Impl") {
  group = "build"
      dependsOn(gradle.includedBuild("p2shared").task(":p2impl:dependencies"))
}
tasks.register("depPlugin") {
  group = "build"
      dependsOn(gradle.includedBuild("p2plugin").task(":dependencies"))
}



tasks.register("depTest") {
  group = "build"
      dependsOn(gradle.includedBuild("p2test").task(":dependencies"))
}
tasks.register("buildTest") {
  group = "build"
      dependsOn(gradle.includedBuild("p2test").task(":build"))
}

tasks.register("depGroovy") {
  group = "build"
      dependsOn(gradle.includedBuild("testGroovy").task(":dependencies"))
}
tasks.register("modelGroovy") {
  group = "build"
      dependsOn(gradle.includedBuild("testGroovy").task(":model"))
}

tasks.register("buildGroovy") {
  group = "build"
      dependsOn(gradle.includedBuild("testGroovy").task(":build"))
      dependsOn(gradle.includedBuild("testGroovy").task(":p2Publish"))
      dependsOn(gradle.includedBuild("testGroovy").task(":hello"))
}
