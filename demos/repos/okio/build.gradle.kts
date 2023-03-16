import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
  kotlin("multiplatform")
  // id("org.danbrough.kotlinxtras.consumer")
}



repositories {
  mavenLocal()
  //maven("/usr/local/kotlinxtras/build/m2")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}



kotlin {


  linuxX64()
  linuxArm64()
  linuxArm32Hfp()
  androidNativeX86()

  if (org.jetbrains.kotlin.konan.target.HostManager.Companion.hostIsMac) {
    macosX64()
    macosArm64()
  }

  /** //uncomment if you want android support
  androidNativeX86()
  androidNativeX64()
  androidNativeArm32()
  androidNativeArm64()

   **/

  //add your other apple targets


  val commonMain by sourceSets.getting {
    dependencies {
      implementation("org.danbrough:klog:_")
      implementation("org.danbrough.okio:okio:_")
      implementation("org.danbrough.okio:okio-fakefilesystem:_")

    }
  }

  val nativeMain by sourceSets.creating {
    dependsOn(commonMain)
  }


  targets.withType<KotlinNativeTarget>().all {

    compilations["main"].apply {
      defaultSourceSet.dependsOn(nativeMain)
    }

    binaries {
      executable("hashing") {
        entryPoint = "okio.samples.main"
        runTask?.workingDir = project.projectDir
      }
    }

  }
}

