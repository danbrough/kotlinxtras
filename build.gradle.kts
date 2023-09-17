import org.danbrough.xtras.XTRAS_REPO_NAME
import org.danbrough.xtras.xtrasMavenDir
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.org.jetbrains.dokka) apply false
  alias(libs.plugins.xtras) apply false
  `maven-publish`
}

val publishingVersion: String = libs.versions.xtrasPublishing.get()
val publishingGroup: String = libs.versions.xtrasPackage.get()

//println("MAVEN DIR: $xtrasMavenDir")

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

fun MavenPublication.xtrasPom() {
  pom {

    name.set("KotlinXtras")
    description.set("Common kotlin packages with linux arm and android native support")
    url.set("https://github.com/danbrough/kotlinxtras/")

    licenses {
      license {
        name.set("Apache-2.0")
        url.set("https://opensource.org/licenses/Apache-2.0")
      }
    }

    scm {
      connection.set("scm:git:git@github.com:danbrough/kotlinxtras.git")
      developerConnection.set("scm:git:git@github.com:danbrough/kotlinxtras.git")
      url.set("https://github.com/danbrough/kotlinxtras/")
    }

    issueManagement {
      system.set("GitHub")
      url.set("https://github.com/danbrough/kotlinxtras/issues")
    }

    developers {
      developer {
        id.set("danbrough")
        name.set("Dan Brough")
        email.set("dan@danbrough.org")
        organizationUrl.set("https://github.com/danbrough")
      }
    }
  }
}





