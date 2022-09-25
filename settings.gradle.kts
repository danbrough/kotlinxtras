pluginManagement {

  repositories {
    maven("/usr/local/kotlinxtras/build/m2")
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.50.2"
}


rootProject.name = "kotlinxtras"

//include(":konandeps")
//include(":openssl")
//include(":curl")
include(":plugin")
