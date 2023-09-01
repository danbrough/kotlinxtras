pluginManagement {

  repositories {
    //for local builds
    maven("/usr/local/kotlinxtras/build/xtras/maven")
    //for unreleased staging builds
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    //for release builds
    
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.51.0"
}

rootProject.name = "sqlite_demo"

