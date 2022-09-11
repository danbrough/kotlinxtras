pluginManagement {

  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.40.2"
}



rootProject.name = "kotlinxtras"

include(":konandeps")
include(":openssl")
include(":curl")
