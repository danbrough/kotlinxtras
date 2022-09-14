import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.Copy
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.konan.target.KonanTarget
import KotlinXtras_gradle.KotlinXtras.binaryTargets
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.Family


plugins {
  `maven-publish`
  id("kotlin-multiplatform")
}


//ProjectProperties.init(project)
val KonanTarget.platformName: String
  get() {
    if (family == Family.ANDROID) {
      return when (this) {
        KonanTarget.ANDROID_X64 -> "androidNativeX64"
        KonanTarget.ANDROID_X86 -> "androidNativeX86"
        KonanTarget.ANDROID_ARM64 -> "androidNativeArm64"
        KonanTarget.ANDROID_ARM32 -> "androidNativeArm32"
        else -> throw Error("Unhandled android target $this")
      }
    }
    return name.split("_").joinToString("") { it.capitalize() }.decapitalize()
  }

repositories {
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  mavenCentral()
}


//download all zipped binaries and extract into the libs directory.
val unzipAll:Task  by tasks.creating{
  group = KotlinXtras.binariesTaskGroup
}


val preCompiled: Configuration by configurations.creating {
  isTransitive = false
}

dependencies {
  setOf("openssl", "curl").forEach { libName ->
    binaryTargets.forEach { target ->
      preCompiled("org.danbrough.kotlinxtras:$libName${target.platformName.capitalized()}:0.0.1-beta01")
    }
  }
}


preCompiled.resolvedConfiguration.resolvedArtifacts.forEach { artifact->
  tasks.register<Copy>("unzip${artifact.name.capitalized()}") {
    group = KotlinXtras.binariesTaskGroup
    from(zipTree(artifact.file).matching {
      exclude("**/META-INF")
      exclude("**/META-INF/*")
    })
    into(project.buildDir.resolve("kotlinxtras"))
  }.also {
    unzipAll.dependsOn(it)
  }
}


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
  const  val binariesTaskGroup = "binaries"

}

project.extensions.getByType<KotlinMultiplatformExtension>().apply {

  /*targets.withType<KotlinNativeTarget>().all {
    compilations["main"]
  }*/

  tasks.withType(KotlinNativeCompile::class).all {
    val konanTarget = KonanTarget.predefinedTargets[target]!!
    dependsOn("unzipCurl${konanTarget.platformName.capitalized()}")
    doFirst {
      println("Doing compile for $konanTarget")
    }
  }
}


