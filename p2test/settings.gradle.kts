pluginManagement {
    repositories {
        maven {
            url =uri("file:../../local-plugin-repository")
        }
        gradlePluginPortal()
       
    }
}
rootProject.name = "p2test"
include("p2testNested")
