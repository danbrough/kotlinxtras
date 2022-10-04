pluginManagement {

  repositories {
    maven(file("build/m2"))
    maven("https://s01.oss.sonatype.org/content/groups/staging")
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

