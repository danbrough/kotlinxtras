
pluginManagement {
  repositories {
    val xtrasRepo = settings.extra.properties["xtrasRepo"].toString()
    println("XTRAS REPO: $xtrasRepo")
    maven(xtrasRepo)
    mavenCentral()
    gradlePluginPortal()
  }
}


plugins {
  id("de.fayard.refreshVersions") version "0.60.3"
  id("org.gradle.toolchains.foojay-resolver-convention") version ("0.7.0")
}

rootProject.name = "demo_mqtt"
