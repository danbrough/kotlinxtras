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
import BuildEnvironment.platformName
import KotlinXtras_gradle.KotlinXtras.binaryTargets

plugins {
  `maven-publish`
}

ProjectProperties.init(project)

repositories {

  maven(Dependencies.SONA_STAGING)
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
    into("libs")
  }.also {
    unzipAll.dependsOn(it)
  }
}


project.task("hello") {
  doLast {
    println("Hello from the KotlinXtras!")
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


