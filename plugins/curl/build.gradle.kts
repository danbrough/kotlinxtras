import org.danbrough.xtras.env.xtrasBuildEnvironment

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
  implementation(libs.xtras.plugin)
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

