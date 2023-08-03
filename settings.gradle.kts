pluginManagement {
  repositories {

    maven(file("build/xtras/maven"))
    //mavenCentral()
    maven("https://repo1.maven.org/maven2")
    mavenCentral()
    maven("https://repo.maven.apache.org/maven2/")
    maven("https://s01.oss.sonatype.org/content/groups/staging")
    gradlePluginPortal()

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
  include(":core")
}

//-PpluginsOnly=false or not specified
if (!pluginsOnly.toBoolean()) {

  include(":common")
  include(":xtras")
}




