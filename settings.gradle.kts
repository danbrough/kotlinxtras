pluginManagement {

  repositories {
    maven(file("build/m2"))
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.50.2"
}


rootProject.name = "kotlinxtras"

include(":plugin")

include(":konandeps")
include(":openssl")
include(":curl")
include(":sqlite")

