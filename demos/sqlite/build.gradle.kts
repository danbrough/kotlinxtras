import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.KonanTarget

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

val binaryDep = project.configurations.create("binary"){
  isVisible = false
  isTransitive = false
  isCanBeConsumed = false
  isCanBeResolved = true
}



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


  tasks.register("resolveBinariesLinuxX64"){
    val konanTarget = org.jetbrains.kotlin.konan.target.KonanTarget.LINUX_X64
    doFirst {
      println("resolving $konanTarget binaries")

      dependencies {
        binaryDep("org.danbrough.kotlinxtras.sqlite.binaries:sqliteLinuxX64:3.39.4")
        binaryDep("org.danbrough.kotlinxtras.sqlite.binaries:sqliteLinuxX22:3.39.4")

      }


      binaryDep.incoming.dependencies.all {
        println("BINDEP DEPENDENCY $this")
      }
      binaryDep.resolutionStrategy {
        this.eachDependency {

        }
      }
      binaryDep.resolve().also {
        println("RESOLVED: $it")
      }

    }
  }

  tasks.withType<KotlinNativeCompile>{
    if (target == "linux_x64"){
     dependsOn("resolveBinariesLinuxX64")
    }
  }

  tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink>{
    if (target == "linux_x64"){
      dependsOn("resolveBinariesLinuxX64")
    }
  }


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



