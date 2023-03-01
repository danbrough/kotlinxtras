pluginManagement {
  repositories {
    maven(file("build/xtras/maven"))
    maven("https://s01.oss.sonatype.org/content/groups/staging")
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.51.0"
}

rootProject.name = "kotlinxtras"

val pluginsOnly: String? by settings

//-PpluginsOnly=true or not specified
if (pluginsOnly == null || pluginsOnly.toBoolean()) {
  include(":plugin")
}

//-PpluginsOnly=false or not specified
if (!pluginsOnly.toBoolean()) {
  include(":core")
  include(":common")
  include(":xtras")
}




