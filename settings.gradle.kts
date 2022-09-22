pluginManagement {

  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.50.1"
}


rootProject.name = "kotlinxtras"

include(":konandeps")
include(":openssl")
include(":curl")
include(":plugin")
