pluginManagement {

  repositories {
    maven("/usr/local/kotlinxtras/build/xtras/maven")
    maven("https://s01.oss.sonatype.org/content/groups/staging")
    gradlePluginPortal()
    mavenCentral()
    google()
  }


}

plugins {
  id("de.fayard.refreshVersions") version "0.51.0"
}


rootProject.name = "curl_demo"




