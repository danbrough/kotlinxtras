import BuildEnvironment.platformName
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform") apply false
  id("io.github.gradle-nexus.publish-plugin")
  `maven-publish`
  signing
  id("KotlinXtras")
}



group = ProjectProperties.projectGroup
version = ProjectProperties.buildVersionName


allprojects {
  repositories {
    maven(Dependencies.SONA_STAGING)
    mavenCentral()
  }
}


val binariesDir = project.buildDir.resolve("binaries")

fun createLibraryJar(target: KonanTarget, libName: String): Jar {
  val jarName = "$libName${target.platformName.capitalized()}Binaries"

  return tasks.create<Jar>("zip${jarName.capitalized()}") {
    archiveBaseName.set(jarName)
    dependsOn(rootProject.getTasksByName("build${target.platformName.capitalized()}", true).first())
    group = KotlinXtras_gradle.KotlinXtras.binariesTaskGroup
    from(project.fileTree("libs/$libName/${target.platformName}")){
      include("include/**","lib/*.so","lib/*.a","lib/*.dll","lib/*.dylib")
    }
    into("$libName/${target.platformName}")
    destinationDirectory.set(binariesDir)
  }
}


publishing {
  publications {
    setOf("curl", "openssl").forEach { libName ->
      KotlinXtras_gradle.KotlinXtras.binaryTargets.forEach { target ->
        create<MavenPublication>("$libName${target.platformName.capitalized()}") {
          artifactId = name
          artifact(createLibraryJar(target, libName))
        }
      }
    }
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

  group = ProjectProperties.projectGroup
  version = ProjectProperties.buildVersionName


  extensions.findByType(PublishingExtension::class) ?: return@allprojects

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
