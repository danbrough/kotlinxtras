import org.danbrough.kotlinxtras.binaries.git
import org.danbrough.kotlinxtras.binaries.registerLibraryExtension
import org.danbrough.kotlinxtras.declareHostTarget
import org.danbrough.kotlinxtras.hostTriplet
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasMavenDir
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform")
  id("org.danbrough.kotlinxtras.binaries")
}

repositories {
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  maven(xtrasMavenDir)
  mavenCentral()
}

group = "demo.uuid"

registerLibraryExtension("uuid") {
  version = "2.38.1"

  publishingGroup = "demo.uuid"

  deferToPrebuiltPackages = false

  git(
    "https://git.kernel.org/pub/scm/utils/util-linux/util-linux.git",
    "54a4d5c3ec33f2f743309ec883b9854818a25e31"
  )

  cinterops {
    headers = """
      headers = uuid/uuid.h 
      #for dynamic linking
      #linkerOpts = -luuid
      #or use this for static linking
      staticLibraries = libuuid.a
      """.trimIndent()
  }

  val autoGenTaskName: KonanTarget.() -> String =
    { "xtrasAutogen${libName.capitalized()}${platformName.capitalized()}" }

  configureTarget { target ->

    project.tasks.create(target.autoGenTaskName(), Exec::class.java) {
      dependsOn(extractSourcesTaskName(target))
      workingDir(sourcesDir(target))

      outputs.file(workingDir.resolve("configure"))
      commandLine("./autogen.sh")
    }
  }

  configure { target ->
    dependsOn(target.autoGenTaskName())
    outputs.file(workingDir.resolve("Makefile"))

    commandLine(
      "./configure",
      "--host=${target.hostTriplet}",
      "--enable-libuuid",
      //"--enable-uuidgen",
      "--disable-all-programs",
      "--prefix=${buildDir(target)}"
    )
  }

  build {
    commandLine(binaries.makeBinary, "install")
  }

}



kotlin {


  declareHostTarget()


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
      executable("uuidDemo") {
        entryPoint = "demo.main"
      }
    }
  }
}


tasks.create("run") {
  dependsOn("runUuidDemoDebugExecutable${HostManager.host.platformName.capitalized()}")
}

