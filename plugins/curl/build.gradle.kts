import org.danbrough.xtras.env.xtrasBuildEnvironment
import org.danbrough.xtras.xtrasBuildDir
import org.danbrough.xtras.xtrasMavenDir

plugins {
  //alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.xtras)
  `kotlin-dsl`
  `maven-publish`
}


xtrasBuildEnvironment {
  javaLanguageVersion = 8
}

group = libs.versions.xtrasPackage.get()
version = libs.versions.curl.get()


dependencies {
  //add("compileOnly", kotlin("gradle-plugin"))
  //add("compileOnly", kotlin("gradle-plugin-api"))
  implementation(libs.xtras.plugin)
  implementation(libs.org.danbrough.klog)
  implementation(libs.kotlin.gradle.plugin)
}


gradlePlugin {
  plugins {
    create("curl") {
      id = "$group.curl"
      implementationClass = "$group.curl.CurlPlugin"
    }
  }
}

