pluginManagement {

  repositories {

    maven("../../build/m2") {
      name = "m2"
    }

    gradlePluginPortal()
    mavenCentral()
    google()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.50.1"
}



rootProject.name = "curl_standalone"

