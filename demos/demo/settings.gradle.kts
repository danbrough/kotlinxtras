pluginManagement {

  repositories {
    maven(file("../build/m2"))
    maven("https://s01.oss.sonatype.org/content/groups/staging")
    gradlePluginPortal()
    mavenCentral()
    google()
  }


}

plugins {
  id("de.fayard.refreshVersions") version "0.51.0"
}


//includeBuild(file("../plugin"))

rootProject.name = "kotlinxtras_demo"



