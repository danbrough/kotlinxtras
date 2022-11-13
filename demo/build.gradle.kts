
import org.danbrough.kotlinxtras.archiveSource
import org.danbrough.kotlinxtras.binaries.CurrentVersions.enableIconv
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.consumer")
  id("org.danbrough.kotlinxtras.iconv")
}

binaries {
  enableIconv()
}

iconv {
  version = "1.17c"

  source = archiveSource("https://ftp.gnu.org/pub/gnu/libiconv/libiconv-1.17.tar.gz") {
    stripTopDir = true
    tarExtractOptions = "xfz"
  }

  //downloadSourcesTask = registerGitDownloadTask("https://github.com/danbrough/openssl", "02e6fd7998830218909cbc484ca054c5916fdc59")
  //downloadSourcesTask = registerSourceDownloadTask("https://ftp.gnu.org/pub/gnu/libiconv/libiconv-1.17.tar.gz", extractOptions = "xvfz",stripTopDir =true)
}

kotlin {

  linuxX64()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
    dependencies {
      implementation(project(":iconv"))
    }
  }

  targets.withType<KotlinNativeTarget>().all {
    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }

    binaries {
      executable("iconvDemo") {
        entryPoint = "demo1.main"
      }
    }
  }

}