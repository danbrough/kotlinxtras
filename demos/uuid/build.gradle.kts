import org.danbrough.kotlinxtras.binaries.git
import org.danbrough.kotlinxtras.binaries.registerLibraryExtension
import org.danbrough.kotlinxtras.hostTriplet
import org.danbrough.kotlinxtras.platformName
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.core")
}

repositories {
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}


registerLibraryExtension("uuid") {
  version = "2.38.1"

  git(
    "https://git.kernel.org/pub/scm/utils/util-linux/util-linux.git",
    "54a4d5c3ec33f2f743309ec883b9854818a25e31"
  )

  cinterops {
    headers = """
      headers = uuid/uuid.h 
      linkerOpts = -luuid 
      """.trimIndent()
  }

  val autoGenTaskName: KonanTarget.() -> String =
    { "xtrasAutogen${libName.capitalized()}${platformName.capitalized()}" }

  configureTarget { target ->
    project.tasks.create(target.autoGenTaskName(), Exec::class.java) {
      dependsOn(extractSourcesTaskName(target))
      workingDir(sourcesDir(target))
      onlyIf { !isPackageBuilt(target) }
      outputs.file(workingDir.resolve("configure"))
      commandLine("./autogen.sh")
    }
  }

  configure { target ->
    dependsOn(target.autoGenTaskName())

    commandLine(
      "./configure",
      "--host=${target.hostTriplet}",
      "--enable-libuuid",
      "--enable-uuidgen",
      "--disable-all-programs",
      "--prefix=${buildDir(target)}"
    )
  }

  build {
    commandLine(binaries.makeBinary, "install")
  }

}


kotlin {

  linuxX64()
  linuxArm32Hfp()
  //macosX64()
  linuxArm64()
  androidNativeX86()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation("org.danbrough:klog:_")
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget> {

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }

    binaries {
      executable("demo") {
        entryPoint = "demo.main"
      }
    }
  }
}
