pluginManagement {

  repositories {
    maven("../../build/m2")
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    gradlePluginPortal()
    mavenCentral()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.51.0"
}



rootProject.name = "ktor_standalone_demo"

