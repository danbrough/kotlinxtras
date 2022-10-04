import   org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
  kotlin("multiplatform") apply false
  `maven-publish`
  id("KotlinXtras")
  signing
  id("org.jetbrains.dokka") apply false
}


group = ProjectProperties.projectGroup
version = ProjectProperties.buildVersionName


allprojects {

  repositories {
    maven( "https://s01.oss.sonatype.org/content/groups/staging/")
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



allprojects {


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


        val sonatypeRepoId = project.properties["sonatypeRepoId"]!!.toString()
        maven("https://s01.oss.sonatype.org/service/local/staging/deployByRepositoryId/$sonatypeRepoId"){
          name = "SonaType"
          credentials{
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

