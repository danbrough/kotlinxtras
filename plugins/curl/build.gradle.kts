
plugins {
  `kotlin-dsl`
  `maven-publish`
  `signing`
  id("org.jetbrains.dokka")
  id("org.danbrough.kotlinxtras.xtras")
  id("org.danbrough.kotlinxtras.sonatype")
}

group = "org.danbrough.kotlinxtras"
version = "7_86_0a"

dependencies {
  compileOnly( kotlin("gradle-plugin"))
  compileOnly( kotlin("gradle-plugin-api"))
  implementation(libs.xtras)
}

gradlePlugin {
  plugins {
    create("curl") {
      id = "$group.curl"
      implementationClass = "$group.CurlPlugin"
      displayName = "KotlinXtras curl plugin"
      description = "Provides curl support to multi-platform projects"
    }
  }
}

