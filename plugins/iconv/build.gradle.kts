
plugins {
  `kotlin-dsl`
  `maven-publish`
  `signing`
  id("org.jetbrains.dokka")
  id("org.danbrough.kotlinxtras.xtras")
  id("org.danbrough.kotlinxtras.sonatype")
}

group = "org.danbrough.kotlinxtras"
version = "1.17c"

dependencies {
  compileOnly( kotlin("gradle-plugin"))
  compileOnly( kotlin("gradle-plugin-api"))
  implementation(libs.xtras)
}

gradlePlugin {

  plugins {
    create("iconv") {
      id = "$group.iconv"
      implementationClass = "$group.IconvPlugin"
      displayName = "KotlinXtras iconv plugin"
      description = "Provides iconv support to multi-platform projects"
    }
  }

}

