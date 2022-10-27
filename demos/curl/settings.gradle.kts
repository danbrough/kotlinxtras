pluginManagement {

  repositories {
    maven("../../build/m2")
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.51.0"
}



rootProject.name = "curl_demo"

