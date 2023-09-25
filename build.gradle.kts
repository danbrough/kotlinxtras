import org.danbrough.xtras.XTRAS_REPO_NAME
import org.danbrough.xtras.xtrasMavenDir
import org.danbrough.xtras.xtrasPom
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.org.jetbrains.dokka) apply false
  alias(libs.plugins.xtras) apply false
  `maven-publish`
}

val publishingVersion: String = libs.versions.xtrasPublishing.get()
val publishingGroup: String = libs.versions.xtrasPackage.get()

allprojects {

  group = publishingGroup
  version = publishingVersion

  repositories {
    maven(xtrasMavenDir) {
      name = XTRAS_REPO_NAME
    }
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
  }


  afterEvaluate {
    //println("PROJECT IS: $name")

    extensions.findByType<PublishingExtension>()?.apply {
      //println("found publishing for $name")
      repositories {
        maven(xtrasMavenDir) {
          name = XTRAS_REPO_NAME
        }
      }
    }
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
  afterEvaluate {
    extensions.findByType(PublishingExtension::class)?.apply {
      publications.all {
        if (this is MavenPublication)
          xtrasPom()
      }
    }
  }
}



