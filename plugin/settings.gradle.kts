pluginManagement {

  repositories {
    maven(file("../build/m2"))
    maven("https://s01.oss.sonatype.org/content/groups/staging")
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}


dependencyResolutionManagement {
  versionCatalogs {
    val libs by creating {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}
