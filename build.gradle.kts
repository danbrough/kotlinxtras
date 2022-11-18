
//import org.danbrough.kotlinxtras.xtrasPom
import org.danbrough.kotlinxtras.xtrasPom
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  kotlin("multiplatform") apply false
  `maven-publish`
  id("org.jetbrains.dokka") apply false
  id("org.danbrough.kotlinxtras.xtras")
  //id("org.danbrough.kotlinxtras.sonatype")

}

println("Using Kotlin compiler version: ${org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION}")


group = "org.danbrough.kotlinxtras"
version = "0.0.2-beta01"


allprojects {

  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
  }

  tasks.withType<AbstractTestTask>() {
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
  //apply<PropertiesPlugin>()

  afterEvaluate {

    //group = ProjectProperties.projectGroup
//    if (version == "unspecified")
//      version = ProjectProperties.buildVersionName

    extensions.findByType(PublishingExtension::class) ?: return@afterEvaluate

    publishing {
      repositories {
        maven(rootProject.buildDir.resolve("m2")) {
          name = "M2"
        }
      }

      publications.all {
        if (this !is MavenPublication) return@all
        //xtrasPom()
        xtrasPom()
      }
    }
  }
}

