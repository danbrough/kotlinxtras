pluginManagement {

  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.50.0"
}



rootProject.name = "kotlinxtras"

include(":konandeps")
include(":openssl")
include(":curl")
