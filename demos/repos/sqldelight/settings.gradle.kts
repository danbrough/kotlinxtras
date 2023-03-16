pluginManagement {

  repositories {
    maven("/usr/local/kotlinxtras/build/m2")
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    maven("https://www.jetbrains.com/intellij-repository/releases")
    maven("https://cache-redirector.jetbrains.com/intellij-dependencies")
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.51.0"

}



rootProject.name = "sqldelight_demo"

