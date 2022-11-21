
plugins {
  `kotlin-dsl`
  `maven-publish`
  `signing`
  id("org.jetbrains.dokka")
  id("org.danbrough.kotlinxtras.xtras")
  id("org.danbrough.kotlinxtras.sonatype")
}

group = "org.danbrough.kotlinxtras"
version = "1_1_1s"

dependencies {
  compileOnly( kotlin("gradle-plugin"))
  compileOnly( kotlin("gradle-plugin-api"))
  implementation(libs.xtras)
}

gradlePlugin {
  plugins {
    create("openssl") {
      id = "$group.openssl"
      implementationClass = "$group.OpenSSLPlugin"
      displayName = "KotlinXtras openssl plugin"
      description = "Provides openssl support to multi-platform projects"
    }
  }
}

