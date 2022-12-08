
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
    maven(file("../build/xtras/maven"))
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}

