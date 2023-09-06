import org.danbrough.xtras.env.xtrasBuildEnvironment
import org.danbrough.xtras.xtrasBuildDir
import org.danbrough.xtras.xtrasMavenDir

plugins {
  //alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.xtras)
  `kotlin-dsl`
}


xtrasBuildEnvironment {
javaLanguageVersion = 8
}

group = libs.versions.xtrasPackage.get()
version = libs.versions.wolfssl.get()

repositories {
  maven(project.xtrasMavenDir)
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  mavenCentral()
}

dependencies {
  //add("compileOnly", kotlin("gradle-plugin"))
  //add("compileOnly", kotlin("gradle-plugin-api"))
  implementation(libs.xtras.plugin)
  implementation(libs.org.danbrough.klog)
  implementation(libs.kotlin.gradle.plugin)
}


gradlePlugin {
  plugins {
    create("wolfssl") {
      id = "$group.wolfssl"
      implementationClass = "$group.wolfssl.WolfSSLPlugin"
    }
  }
}

