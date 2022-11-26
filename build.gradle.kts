
//import org.danbrough.kotlinxtras.xtrasPom
import org.danbrough.kotlinxtras.xtrasDir
import org.danbrough.kotlinxtras.xtrasPom
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  kotlin("multiplatform") apply false
  `maven-publish`
  id("org.jetbrains.dokka") apply false
  id("org.danbrough.kotlinxtras.binaries")
  id("org.danbrough.kotlinxtras.sonatype") apply false
}

println("Using Kotlin compiler version: ${org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION}")

val xtrasGroup:String  = project.property("project.group")!!.toString()
group = xtrasGroup


println("XTRAS_DIR: ${project.xtrasDir}")

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

