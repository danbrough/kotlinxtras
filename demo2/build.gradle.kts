import org.danbrough.kotlinxtras.binaries.LibraryExtension
import org.danbrough.kotlinxtras.binaries.download
import org.danbrough.kotlinxtras.binaries.registerLibraryExtension
import org.danbrough.kotlinxtras.hostTriplet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
  xtras("binaries")
}

open class Demo2Extension(_project: Project) : LibraryExtension(_project, "demo2")

project.registerLibraryExtension("demo2",Demo2Extension::class.java){
  libName = "demo2"
  buildEnabled = true
  version = "0.0.1"

  download("https://ftp.gnu.org/pub/gnu/libiconv/libiconv-1.17.tar.gz") {
    stripTopDir = true
  }

  configure { target ->
    commandLine(
      "./configure",
      "-C",
      "--enable-static",
      "--host=${target.hostTriplet}",
      "--prefix=${buildDir(target)}"
    )
    outputs.file(workingDir.resolve("Makefile"))
  }

  build {
    commandLine(binaries.makeBinary, "install")
  }

  cinterops {
    headers = """
          |headers = iconv.h libcharset.h
          |linkerOpts = -liconv
          |package = libiconv
          |""".trimMargin()
  }
}




repositories {
  mavenCentral()
}


kotlin {
  linuxX64()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation("org.danbrough:klog:_")
      implementation(project(":common"))
    }
  }

  val posixMain by sourceSets.creating{
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget> {

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }


    binaries {
      executable("demo2") {
        entryPoint = "demo1.main"
      }
    }
  }
}

