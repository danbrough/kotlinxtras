import Xtras.xtrasPom
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  kotlin("multiplatform") apply false
  `maven-publish`
  id("org.jetbrains.dokka") apply false
  alias(libs.plugins.org.danbrough.kotlinxtras.binaries)
  alias(libs.plugins.org.danbrough.kotlinxtras.sonatype) apply false
}

println("Using Kotlin compiler version: ${org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION}")

val xtrasGroup = Xtras.projectGroup
val xtrasVersion = libs.versions.xtras.get()

group = Xtras.projectGroup

allprojects {

  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
  }

  tasks.withType<AbstractTestTask> {
    testLogging {
      events = setOf(
        TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED
      )
      exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
      showStandardStreams = true
      showStackTraces = true
    }
    outputs.upToDateWhen {
      false
    }
  }
}


subprojects {
  group = xtrasGroup
  version = xtrasVersion

  afterEvaluate {
    extensions.findByType(PublishingExtension::class)?.also {
      publishing {
        publications.all {
          if (this !is MavenPublication) return@all
          xtrasPom()
        }
      }
    }
  }
}

