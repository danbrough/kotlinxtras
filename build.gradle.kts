import BuildEnvironment.platformName
import Curl.curlPrefix
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

plugins {
  kotlin("multiplatform") apply false
  id("io.github.gradle-nexus.publish-plugin")
  `maven-publish`
  signing
}

ProjectProperties.init(project)

group = ProjectProperties.projectGroup
version = ProjectProperties.buildVersionName


allprojects {
  repositories {
    maven(Dependencies.SONA_STAGING)
    mavenCentral()
  }
}



val binariesGroup = "binaries"

fun createLibraryJar(target: KonanTarget, libName: String): Jar {
  val jarName = "$libName${target.platformName.capitalized()}Binaries"

  return tasks.create<Jar>("${jarName}Jar") {
    archiveBaseName.set(jarName)
    group = binariesGroup
    from(project.file("libs").resolve(libName).resolve(target.platformName))

    include("include/**")
    include("lib/*.so")
    include("lib/*.dll")
    include("lib/*.dylib")
    include("lib/*.a")

    destinationDirectory.set(project.buildDir.resolve("jars"))
  }
}


publishing {
  publications {
    setOf("curl", "openssl").forEach { libName ->
      setOf(
        KonanTarget.LINUX_ARM32_HFP,
        KonanTarget.LINUX_ARM64,
        KonanTarget.LINUX_X64,
        KonanTarget.ANDROID_X86,
        KonanTarget.ANDROID_X64,
        KonanTarget.ANDROID_ARM32,
        KonanTarget.ANDROID_ARM64,
      ).forEach { target ->
        create<MavenPublication>("$libName${target.platformName.capitalized()}Binaries") {
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
/*
  signing {
    sign(publishing.publications)
  }*/
  group = ProjectProperties.projectGroup
  version = ProjectProperties.buildVersionName

  afterEvaluate {
    extensions.findByType(PublishingExtension::class) ?: return@afterEvaluate

    publishing {


      repositories {
        maven(rootProject.buildDir.resolve("m2")) {
          name = "m2"
        }
      }

      publications.all {
        if (this !is MavenPublication) return@all

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
