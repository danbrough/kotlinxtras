pluginManagement {

  repositories {
    //maven(file("build/m2"))
    maven("https://s01.oss.sonatype.org/content/groups/staging")
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}



plugins {
  id("de.fayard.refreshVersions") version "0.51.0"
}


rootProject.name = "kotlinxtras"


//include(":common")
//include(":demo")
//include(":iconv")

//include(":openssl")
//include(":curl")
//include(":sqlite")
//include(":plugin2")
include(":iconvPlugin")

//includeBuild("plugin_old")
//include("plugin2")
//includeBuild("commonBuild")
includeBuild("plugin")
