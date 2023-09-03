pluginManagement {
  repositories {
    maven(file("build/xtras/maven"))
    maven("https://s01.oss.sonatype.org/content/groups/staging")
    mavenCentral()
    gradlePluginPortal()
    google()
  }
}

plugins {
  id("de.fayard.refreshVersions") version "0.60.2"
  id("org.gradle.toolchains.foojay-resolver-convention") version ("0.7.0")
}

val publish: String? by settings


rootProject.name = "kotlinxtras"

//includeBuild("plugin")


include(":plugin")

//project(":plugin").projectDir = rootDir.resolve("plugin2")
//include(":core:wolfssl")
if (publish == null || publish == "core") {
  listOf(
    "curl",
   "wolfssl",
    "wolfssh",
  ).forEach {
    include(":$it")
    project(":$it").projectDir = rootDir.resolve("core/$it")
  }
}

if (publish == null || publish == "binaries")
  include(":binaries")

if (publish == null || publish == "demos"){
  listOf("curl").forEach {
    include(":demos:${it}_demo")
    project(":demos:${it}_demo").projectDir = rootDir.resolve("demos/$it")
  }
}