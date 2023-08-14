pluginManagement {
  repositories {
    maven(file("build/xtras/maven"))
    maven("https://s01.oss.sonatype.org/content/groups/staging")
    mavenCentral()
    gradlePluginPortal()
    google()
  }
}



plugins {
  id("de.fayard.refreshVersions") version "0.51.0"
////                          # available:"0.52.0-SNAPSHOT"
}

rootProject.name = "kotlinxtras"

val pluginsOnly: String? by settings

//-PpluginsOnly=true or not specified
if (pluginsOnly == null || pluginsOnly.toBoolean()) {
  include(":plugin")
  include(":core")
}

//-PpluginsOnly=false or not specified
if (!pluginsOnly.toBoolean()) {
  include(":libssh2")
  include(":utils")
  include(":common")
  include(":binaries")
}




