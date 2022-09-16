import BuildEnvironment.platformName
import KotlinXtras_gradle.KotlinXtras.binaryTargets
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  `maven-publish`
}

ProjectProperties.init(project)

repositories {
  maven(Dependencies.SONA_STAGING)
}


//download all zipped binaries and extract into the libs directory.
val unzipAll: Task by tasks.creating {
  group = KotlinXtras.binariesTaskGroup
}


//val preCompiled: Configuration by configurations.creating {
//  isTransitive = false
//}
//
//dependencies {
//  setOf("openssl", "curl").forEach { libName ->
//    binaryTargets.forEach { target ->
//      preCompiled("org.danbrough.kotlinxtras:$libName${target.platformName.capitalized()}:0.0.1-beta01")
//    }
//  }
//}
//
//
//preCompiled.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
//  tasks.register<Copy>("unzip${artifact.name.capitalized()}") {
//    group = KotlinXtras.binariesTaskGroup
//    from(zipTree(artifact.file).matching {
//      exclude("**/META-INF")
//      exclude("**/META-INF/*")
//    })
//    into("libs")
//  }.also {
//    unzipAll.dependsOn(it)
//  }
//}




object KotlinXtras {
  val binaryTargets = setOf(
    KonanTarget.LINUX_ARM32_HFP,
    KonanTarget.LINUX_ARM64,
    KonanTarget.LINUX_X64,
    KonanTarget.ANDROID_X86,
    KonanTarget.ANDROID_X64,
    KonanTarget.ANDROID_ARM32,
    KonanTarget.ANDROID_ARM64,
  )

  const val binariesTaskGroup = "binaries"


  fun Project.configureBinarySupport() {
    val libName = name
    val versionName = version.toString()

    publishing {
      repositories {
        maven(rootProject.buildDir.resolve("m2")) {
          name = "m2"
        }
      }
    }

    binaryTargets.forEach { target ->
      val jarName = "$libName${target.platformName.capitalized()}"

      val jarTask = tasks.register<Jar>("zip${jarName.capitalized()}Binaries") {
        archiveBaseName.set(jarName)
        dependsOn("build${target.platformName.capitalized()}")
        group = KotlinXtras_gradle.KotlinXtras.binariesTaskGroup
        from(rootProject.fileTree("libs/$libName/${target.platformName}")) {
          include("include/**", "lib/*.so", "lib/*.a", "lib/*.dll", "lib/*.dylib")
        }
        into("$libName/${target.platformName}")
        destinationDirectory.set(rootProject.buildDir.resolve("binaries"))
      }

      publishing.publications.register<MavenPublication>("$libName${target.platformName.capitalized()}Binaries") {
        artifactId = name
        version = versionName
        artifact(jarTask)
      }
    }
  }

}


