pluginManagement {
  repositories {
    maven("https://maven.danbrough.org")

    mavenCentral()
    gradlePluginPortal()
  }
}

plugins {
  id("de.fayard.refreshVersions") version "0.60.3"
  id("org.gradle.toolchains.foojay-resolver-convention") version ("0.8.0")
}

dependencyResolutionManagement {
  versionCatalogs {
    create("xtras") {
      from(files("gradle/libs.versions.toml"))
    }
  }
}

rootProject.name = "xtras2"

listOf("plugin", "support").forEach { plugin ->
  includeBuild(file("plugins/$plugin")) {
    dependencySubstitution {
      substitute(module("org.danbrough.xtras:$plugin")).using(project(":"))
    }
  }
}


val xtrasPlugins = settings.extra["plugins"].toString().split(",")

xtrasPlugins.forEach {
  includeBuild("plugins/$it") {
    name = "${it}_plugin"
    dependencySubstitution {
      substitute(module("org.danbrough.xtras:${it}_plugin")).using(project(":"))
    }
  }
  include(":$it")
  project(":$it").projectDir = rootDir.resolve("libs/$it")
}


//include(":demo_mqtt")
//project(":demo_mqtt").projectDir=rootDir.resolve("demos/mqtt")

//include(":demo")