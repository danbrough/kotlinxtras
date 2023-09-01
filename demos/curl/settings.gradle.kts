pluginManagement {
  repositories {
    maven("/usr/local/kotlinxtras/build/xtras/maven")
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

rootProject.name = "curl_demo"

dependencyResolutionManagement {
  versionCatalogs {
    create("libs${rootProject.name.replace("_", "")}") {
      from(files("../../gradle/libs.versions.toml"))
    }

  }
}
