plugins {
  //alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras)
  `kotlin-dsl`
}

version = libs.versions.wolfssl.get()

dependencies {
  implementation(project(":plugin"))
}

gradlePlugin {
  plugins {
    create("wolfssl") {
      id = "$group.wolfssl"
      implementationClass = "$group.wolfssl.WolfSSLPlugin"
    }
  }
}

