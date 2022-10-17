
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  kotlin("multiplatform") apply false
  `maven-publish`
  id("org.jetbrains.dokka") apply false
  id("org.danbrough.kotlinxtras.properties")
  id("org.danbrough.kotlinxtras.sonatype")

}

//val projectProperties = project.projectProperties
//ProjectProperties.init(project)

group = ProjectProperties.projectGroup
version = ProjectProperties.buildVersionName



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

  tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile::class).all {
    kotlinOptions {
      jvmTarget = "11"
    }
  }

  tasks.withType(JavaCompile::class) {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()
  }
}


subprojects {


  afterEvaluate {

    group = ProjectProperties.projectGroup
    if (version == "unspecified")
      version = ProjectProperties.buildVersionName

    extensions.findByType(PublishingExtension::class) ?: return@afterEvaluate

    publishing {
      repositories {
        maven(rootProject.buildDir.resolve("m2")) {
          name = "M2"
        }
      }

      publications.all {
        if (this !is MavenPublication) return@all

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
    }
  }
}

