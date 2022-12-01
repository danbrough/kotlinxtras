
import org.danbrough.kotlinxtras.projectProperty
import org.danbrough.kotlinxtras.xtrasPom
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  kotlin("multiplatform") apply false
  `maven-publish`
  id("org.jetbrains.dokka") apply false
  alias(libs.plugins.org.danbrough.kotlinxtras.binaries) apply false
  alias(libs.plugins.org.danbrough.kotlinxtras.sonatype) apply false
}

println("Using Kotlin compiler version: ${org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION}")

val xtrasGroup = projectProperty<String>("project.group")
group = xtrasGroup

allprojects {

  repositories {
    maven(rootProject.buildDir.resolve("m2"))
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

  afterEvaluate {
    version = libs.versions.xtras.get()
    extensions.findByType(PublishingExtension::class) ?: return@afterEvaluate

    publishing {
      publications.all {
        if (this !is MavenPublication) return@all
        xtrasPom()
      }
    }
  }
}

