
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
   //maven("/usr/local/kotlinxtras/build/m2")
    maven(file("../build/m2"))
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}

