
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {

  kotlin("multiplatform")  apply false
  `maven-publish`
  id("org.jetbrains.dokka") apply false
  xtras("sonatype") version Xtras.version apply false
}

println("Using Kotlin compiler version: ${org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION}")

val xtrasVersion = "0.0.3-beta02"

group = Xtras.projectGroup

allprojects {

  repositories {
    //maven("https://s01.oss.sonatype.org/content/groups/staging/")
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
  group = Xtras.projectGroup
  version = Xtras.publishingVersion


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

