import org.danbrough.kotlinxtras.BuildEnvironment.declareNativeTargets
import org.danbrough.kotlinxtras.platformName
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")

}

kotlin {

  val nativeMain by sourceSets.creating {}

  declareNativeTargets()

  targets.withType<KotlinNativeTarget>().all {
    compilations["main"].apply {
      defaultSourceSet.dependsOn(nativeMain)
    }

    binaries.executable("helloWorld")

    tasks.create(konanTarget.platformName){
      dependsOn("compileKotlin${konanTarget.platformName.capitalize()}")
    }
  }
}