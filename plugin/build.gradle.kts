import org.danbrough.kotlinxtras.projectProperty
import org.danbrough.kotlinxtras.xtrasPom
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  `kotlin-dsl`
  `maven-publish`
  alias(libs.plugins.org.jetbrains.dokka)
  alias(libs.plugins.org.danbrough.kotlinxtras.sonatype)
  alias(libs.plugins.org.danbrough.kotlinxtras.binaries)
}

repositories {
  mavenCentral()
}

val publishingVersion = "0.0.3-beta05"
group = "org.danbrough.kotlinxtras"
version = publishingVersion

dependencies {
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("gradle-plugin-api"))
  compileOnly("org.jetbrains.dokka:dokka-gradle-plugin:${libs.versions.dokka.get()}")
}

sonatype {
  localRepoLocation = project.file("../build/m2")

  configurePublishing {
    publications.all {
      if (this is MavenPublication) {
        version = publishingVersion
        xtrasPom()
      }
    }
  }
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(11))
  }
}

tasks.withType<KotlinJvmCompile> {
  kotlinOptions {
    jvmTarget = "11"
  }
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



