import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget


plugins {
  kotlin("multiplatform")
 // id("org.danbrough.kotlinxtras.consumer")
}



repositories {
  //maven("/usr/local/kotlinxtras/build/m2")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}



kotlin {


  linuxX64()
  linuxArm64()
  androidNativeX86()

  /** //uncomment if you want android support
  androidNativeX86()
  androidNativeX64()
  androidNativeArm32()
  androidNativeArm64()

   **/

  //add your other apple targets


  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation("org.danbrough.okio:okio:3.2.0")
      implementation("org.danbrough.okio:okio-fakefilesystem:3.2.0")

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

