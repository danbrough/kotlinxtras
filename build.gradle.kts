import BuildEnvironment.platformName
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform") apply false
  id("io.github.gradle-nexus.publish-plugin")
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
}

nexusPublishing {
  repositories {
    sonatype {
      nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
      snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
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
        maven(rootProject.buildDir.resolve("m2")) {
          name = "m2"
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
