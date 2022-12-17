
/*dependencyResolutionManagement {
  versionCatalogs {
    val libs by creating {
      from(files("../gradle/libs.versions.toml"))
    }
  }
}*/
pluginManagement {

  repositories {
   maven("https://s01.oss.sonatype.org/content/groups/staging/")
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}

