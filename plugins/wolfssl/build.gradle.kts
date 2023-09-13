import org.danbrough.xtras.env.xtrasBuildEnvironment
import org.danbrough.xtras.xtrasBuildDir
import org.danbrough.xtras.xtrasMavenDir

plugins {
  id("org.danbrough.xtras")
  `kotlin-dsl`
  `maven-publish`
}


xtrasBuildEnvironment {
  javaLanguageVersion = 8
}

group = libs.versions.xtrasPackage.get()
version = libs.versions.wolfssl.get()

dependencies {
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

