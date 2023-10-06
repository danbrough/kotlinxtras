pluginManagement {
  repositories {
    val xtrasMavenDir = settings.extra.properties.let { properties ->
      properties.getOrDefault("xtras.dir.maven", null)?.toString()
        ?: properties.getOrDefault("xtras.dir", null)
          ?.toString()?.let { File(it).resolve("maven").absolutePath }
        ?: error("Gradle property xtras.dir is not set.")
    }

    maven(xtrasMavenDir)
    maven("https://s01.oss.sonatype.org/content/groups/staging")
    mavenCentral()
    gradlePluginPortal()
    google()
  }
}

plugins {
  id("de.fayard.refreshVersions") version "0.60.3"
  id("org.gradle.toolchains.foojay-resolver-convention") version ("0.7.0")
}

val include: String? by settings

rootProject.name = "xtras"


includeBuild("plugin")
//includeBuild("plugin_settings")


if (include == null || include == "plugins") {
  listOf(
    //"wolfssl",
    "curl",
    // "wolfssh",
    "openssl",
    "mqtt",
  ).forEach {
    include(":plugins:$it")
    project(":plugins:$it").projectDir = rootDir.resolve("plugins/$it")
  }
}

if (include == null || include == "binaries") {
  include(":binaries")
  //include(":test")
}
//
//if (include == null || include == "demos") {
//  //include(":test")
//
//  listOf(
//    //"curl",
//    "mqtt",
//  ).forEach {
//    include(":demos:${it}_demo")
//    project(":demos:${it}_demo").projectDir = rootDir.resolve("demos/$it")
//  }
//}
//
//
