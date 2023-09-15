pluginManagement {
  repositories {
    maven("/usr/local/xtras/maven")
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

val include: String? by settings

rootProject.name = "xtras"


includeBuild("plugin")
//includeBuild("plugin_settings")


if (include == null || include == "plugins") {
  listOf(
   "wolfssl",
    "curl",
   // "wolfssh",
  //  "openssl",
  ).forEach {
    include(":plugins:$it")
    project(":plugins:$it").projectDir = rootDir.resolve("plugins/$it")
  }
}

if (include == null || include == "binaries")
  include(":binaries")



if (include == null || include == "demos"){
include(":test")

  listOf("curl").forEach {
    include(":demos:${it}_demo")
    project(":demos:${it}_demo").projectDir = rootDir.resolve("demos/$it")
  }
}


