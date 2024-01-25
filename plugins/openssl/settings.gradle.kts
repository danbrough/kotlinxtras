pluginManagement {
  repositories {
    maven("https://maven.danbrough.org")
    mavenCentral()
    gradlePluginPortal()
  }
}


plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version ("0.7.0")
}

dependencyResolutionManagement {
  versionCatalogs {
    create("xtras") {
      from(files("../../gradle/libs.versions.toml"))
    }
  }
}

rootProject.name = "plugin"
