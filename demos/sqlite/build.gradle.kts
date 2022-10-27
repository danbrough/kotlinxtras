import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.KonanTarget.Companion.predefinedTargets

plugins {
  kotlin("multiplatform")
  //id("org.danbrough.kotlinxtras.consumer")
}


repositories {
  //for local builds
  maven("/usr/local/kotlinxtras/build/m2")
  //for unreleased staging builds
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  //for release builds
  mavenCentral()
}

//  val preCompiled = configurations.getting
//  dependencies {
//    preCompiled(libs.sqlite)
//  }


kotlin {

  linuxX64()
  linuxArm64()
  linuxArm32Hfp()
//  if you want them
//  androidNativeX86()
//  androidNativeX64()
//  androidNativeArm32()
//  androidNativeArm64()


  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.sqlite)
    }
  }

  val nativeMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  val linuxX64Main by sourceSets.getting {

    dependencies {


    }
  }

  targets.withType<KotlinNativeTarget>().all {


    compilations["main"].apply {

      defaultSourceSet.dependsOn(nativeMain)
    }

    binaries {
      executable("sqliteDemo1") {
        println("LINK TASK: $linkTask type; ${linkTask::class.java}")
        entryPoint = "demo1.main"
        runTask?.apply {
          properties["message"]?.also {
            args(it.toString())
          }
        }
      }
    }
  }
}

fun resolveBinariesTask(konanTarget: KonanTarget) =
  tasks.register("resolveBinaries${konanTarget.platformName.capitalized()}") {
    val binaryDep =
      project.configurations.create("binaries${konanTarget.platformName.capitalized()}") {
        isVisible = false
        isTransitive = false
        isCanBeConsumed = false
        isCanBeResolved = true
      }



    dependencies {
      binaryDep("org.danbrough.kotlinxtras.sqlite.binaries:sqlite${konanTarget.platformName.capitalized()}:3.39.4")
      binaryDep("org.danbrough.kotlinxtras.openssl.binaries:openssl${konanTarget.platformName.capitalized()}:1_1_1r")
    }

    //dependsOn(binaryDep)
    println("binDEp: ${binaryDep.name}")
    outputs.files(binaryDep.resolve())
    val taskOutputs = outputs
    //

    doFirst {
      println("resolving $konanTarget binaries")
//        taskOutputs.files(binaryDep.resolve())
      //println("binaries: ${bin}")
//      binaryDep.resolve().also {
//        println("RESOLVED: $it")
//        outputs.file(it)
//      }
    }

    doLast {
      println("FINISHED RESOLVING STUFF")
      println("OUTPUTS: ${this@register.outputs.files.files}")
    }
  }


fun binariesTask(konanTarget: KonanTarget) {
  val resolveTask = resolveBinariesTask(konanTarget)
  tasks.register("extractBinaries${konanTarget.platformName.capitalized()}") {
    dependsOn(resolveTask)
    doFirst {
      println("BINARIES TASK on ${resolveTask.get().outputs.files.files}")
    }
  }

}


tasks.withType<KotlinNativeCompile>().map { KonanTarget.Companion.predefinedTargets[it.target]!! }
  .distinct().forEach(::binariesTask)

tasks.withType<KotlinNativeCompile> {
  dependsOn("extractBinaries${predefinedTargets[target]!!.platformName.capitalized()}")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink> {
  dependsOn("extractBinaries${predefinedTargets[target]!!.platformName.capitalized()}")
}

val KonanTarget.platformName: String
  get() {
    if (family == org.jetbrains.kotlin.konan.target.Family.ANDROID) {
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



