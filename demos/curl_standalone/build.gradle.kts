import org.danbrough.kotlinxtras.Repositories
import org.danbrough.kotlinxtras.configurePrecompiledBinaries
import org.danbrough.kotlinxtras.platformName
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.xtras")
}



repositories {

  maven(Repositories.SONA_STAGING)

  mavenCentral()

}

xtras {
  autoExtractBinaries = true
}


kotlin {

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()
  androidNativeX86()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)

      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.curl)
      implementation(libs.openssl)

    }
  }
  val nativeMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget>().all {

    compilations["main"].defaultSourceSet.dependsOn(nativeMain)

    binaries {
      executable("demo1") {
        entryPoint = "demo1.main"
      }
    }
  }

}

afterEvaluate {

  val binaryDeps: Configuration by configurations.creating {
    isTransitive = false
  }

  val supportedTargets =
    setOf(KonanTarget.LINUX_ARM64, KonanTarget.LINUX_ARM32_HFP, KonanTarget.LINUX_X64)


  dependencies {
    mapOf("curl" to "curl-7_85_0", "openssl" to "OpenSSL_1_1_1q").forEach { dep ->
      setOf("curl", "openssl").forEach {
        supportedTargets.forEach { target->
          binaryDeps("org.danbrough.kotlinxtras:${dep.key}${target.platformName.capitalized()}Binaries:${dep.value}")
        }
        project.tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile::class).forEach {
          val konanTarget = KonanTarget.predefinedTargets[it.target]!!
          it.dependsOn("extract${dep.key.capitalized()}${konanTarget.platformName.capitalized()}Binaries")
        }
      }
    }
  }

  binaryDeps.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
    val unZipTask = tasks.register<Copy>("extract${artifact.name.capitalized()}") {
      group = org.danbrough.kotlinxtras.xtrasTaskGroup
      from(zipTree(artifact.file).matching {
        exclude("**/META-INF")
        exclude("**/META-INF/*")
      })
      into(project.rootProject.buildDir.resolve("kotlinxtras"))
    }
  }

}


