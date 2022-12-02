pluginManagement {

  repositories {
    maven(file("build/m2"))
    maven("https://s01.oss.sonatype.org/content/groups/staging")
    gradlePluginPortal()
    mavenCentral()
    google()
  }


  //includeBuild("./plugin")
}


plugins {
  id("de.fayard.refreshVersions") version "0.51.0"
}

rootProject.name = "kotlinxtras"
val pluginsOnly: String by settings

include(":plugin")
include(":plugins")

if (!pluginsOnly.toBoolean()) {
  include(":common")
  include(":binaries")
}



