plugins {
  `kotlin-dsl`
  `maven-publish`
  alias(libs.plugins.org.jetbrains.dokka)
  alias(libs.plugins.kotlinxtras.sonatype)
}


dependencies {
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("gradle-plugin-api"))
  compileOnly(libs.dokka.gradle.plugin)
}

gradlePlugin {
  plugins {
    create("binariesPlugin") {
      id = "${group}.binaries"
      implementationClass = "$group.binaries.BinaryPlugin"
      displayName = "Xtras Binaries Plugin"
      description = "Provides native library support to Kotlin applications"
    }

    create("sonatypePlugin") {
      id = "$group.sonatype"
      implementationClass = "$group.sonatype.SonatypePlugin"
      displayName = "Sonatype plugin"
      description = "Sonatype publishing support"
    }
  }
}


