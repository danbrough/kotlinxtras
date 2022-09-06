import BuildEnvironment.platformName
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
}

kotlin {

  val nativeMain by sourceSets.creating {}

  linuxX64()
  linuxArm32Hfp()
  linuxArm64()
  mingwX64()
  macosArm64()
  macosX64()
  androidNativeX86()

  targets.withType<KotlinNativeTarget>().all {
    compilations["main"].apply {
      defaultSourceSet.dependsOn(nativeMain)
    }

    binaries {
      executable("helloWorld") {

      }
    }

    tasks.create(konanTarget.platformName){
      dependsOn("linkHelloWorldDebugExecutable${konanTarget.platformName.capitalize()}")
    }
  }
}