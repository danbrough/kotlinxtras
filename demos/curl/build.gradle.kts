import org.danbrough.kotlinxtras.curl.xtrasCurl
import org.danbrough.kotlinxtras.declareHostTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras.curl)
}

repositories {
  maven("/usr/local/kotlinxtras/build/xtras/maven")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
  gradlePluginPortal()
  google()
}


xtrasCurl {
  println("xtrasCurl.supportedTargets = $supportedTargets version:$sourceConfig")
}


kotlin {
  jvm()
  declareHostTarget()
}




