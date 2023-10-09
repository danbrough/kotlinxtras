import org.danbrough.xtras.declareSupportedTargets
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.Family


plugins {
  alias(libs.plugins.kotlinMultiplatform)
  `maven-publish`
}


version = "0.0.1-beta01"

kotlin {

  declareSupportedTargets()
  jvm()

  val commonMain by sourceSets.getting

  targets.withType<KotlinNativeTarget>().all {
    compilations["main"].apply {
      if (konanTarget.family != Family.ANDROID) {
        cinterops.create("jni") {
          defFile = project.file("src/jni.def")
          packageName("platform.android")
          if (konanTarget.family.isAppleFamily) {
            includeDirs(project.file("src/include"))
            includeDirs(project.file("src/include/darwin"))
          } else if (konanTarget.family == Family.MINGW) {
            includeDirs(project.file("src/include"))
            includeDirs(project.file("src/include/win32"))
          } else if (konanTarget.family == Family.LINUX) {
            includeDirs(project.file("src/include"))
            includeDirs(project.file("src/include/linux"))
          }
        }
      }
    }
  }


}


