import org.danbrough.kotlinxtras.binaries.CurrentVersions.enableIconv
import org.danbrough.kotlinxtras.binaries.CurrentVersions.enableSqlite
import org.danbrough.kotlinxtras.platformName
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager


plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.properties")
}
//
//binaries {
//  enableIconv()
//}

repositories {
  //maven("/usr/local/kotlinxtras/build/m2")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}



kotlin {

  //androidNativeX86()
  linuxX64()

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
      //implementation(libs.iconv)

    }
  }


//  val nativeMain by sourceSets.creating {
//    dependsOn(commonMain)
//  }

  targets.withType<KotlinNativeTarget>().all {

    compilations["main"].apply {
      cinterops.create("libiconv") {
        packageName("libiconv")
        defFile(file("src/libiconv.def"))
        extraOpts("-verbose")
      }
    }

    binaries {
      executable("demo1") {
        entryPoint = "demo1.main"
      }
    }

    binaries {
      executable("demo2") {
        entryPoint = "demo2.main"
      }
    }
  }
}

