import org.gradle.api.publish.maven.MavenPublication

object Xtras {
  const val projectGroup = "org.danbrough.kotlinxtras"
  const val version = "0.0.3-beta12"
  const val publishingVersion = "0.0.3-beta12"


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

}