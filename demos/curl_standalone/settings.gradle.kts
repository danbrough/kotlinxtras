pluginManagement {

  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    gradlePluginPortal()
    mavenCentral()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.50.1"
}



rootProject.name = "curl_standalone"

