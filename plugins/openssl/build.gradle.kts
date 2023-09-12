plugins {
  //alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras)
  `kotlin-dsl`
  `maven-publish`
}

version = libs.versions.openssl.get()

dependencies {
  implementation(project(":plugin"))
}

gradlePlugin {
  plugins {
    create("openssl") {
      id = "$group.openssl"
      implementationClass = "$group.openssl.OpenSSLPlugin"
    }
  }
}

