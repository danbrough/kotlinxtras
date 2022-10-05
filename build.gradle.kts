import   org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  kotlin("multiplatform") apply false
  `maven-publish`
  signing
  id("org.jetbrains.dokka") apply false
  id("org.danbrough.kotlinxtras.properties")
  id("org.danbrough.kotlinxtras.sonatype")

}

//val projectProperties = project.projectProperties
ProjectProperties.init(project)

group = ProjectProperties.projectGroup
version = ProjectProperties.buildVersionName


allprojects {

  repositories {
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
  }

  tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class) {
    kotlinOptions {
      jvmTarget = ProjectProperties.KOTLIN_JVM_VERSION
    }
  }

  tasks.withType<JavaCompile>().all {
    sourceCompatibility = JavaVersion.VERSION_11.toString()
    targetCompatibility = JavaVersion.VERSION_11.toString()
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


  apply<SigningPlugin>()

  afterEvaluate {

    group = ProjectProperties.projectGroup
    if (version == "unspecified")
      version = ProjectProperties.buildVersionName

    extensions.findByType(PublishingExtension::class) ?: run {
      //println("PROJECT $name has no publishing")
      return@afterEvaluate
    }

    publishing {
      repositories {
        maven(rootProject.buildDir.resolve("m2")) {
          name = "M2"
        }
        val sonatypeRepositoryId = project.properties["sonatypeRepositoryId"]!!.toString()
        maven("https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/$sonatypeRepositoryId") {
          name = "SonaType"
          credentials {
            username = project.properties["sonatypeUsername"]!!.toString()
            password = project.properties["sonatypePassword"]!!.toString()
          }
        }
      }

      publications.all {
        if (this !is MavenPublication) return@all

        if (project.hasProperty("signPublications"))
          signing {
            sign(this@all)
          }


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

