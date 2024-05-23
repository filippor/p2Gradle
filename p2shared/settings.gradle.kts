rootProject.name = "p2shared"
include("p2api")
include("p2impl")

pluginManagement {
  plugins {
    id ("biz.aQute.bnd.builder") version "7.0.0"
  }
}