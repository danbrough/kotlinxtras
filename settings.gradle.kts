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
////                          # available:"0.51.0-SNAPSHOT"
}


rootProject.name = "kotlinxtras"

include(":plugin")

include(":konandeps")
include(":openssl")
include(":curl")
include(":sqlite")

include(":iconv")
