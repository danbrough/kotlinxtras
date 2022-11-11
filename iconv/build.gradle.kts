

import org.danbrough.kotlinxtras.BuildEnvironment.buildEnvironment
import org.danbrough.kotlinxtras.BuildEnvironment.declareNativeTargets
import org.danbrough.kotlinxtras.BuildEnvironment.hostTriplet
import org.danbrough.kotlinxtras.binaries.CurrentVersions
import org.danbrough.kotlinxtras.createDownloadTask
import org.danbrough.kotlinxtras.konanDepsTaskName
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.sonatype.generateInterops
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform")
  `maven-publish`
  id("org.danbrough.kotlinxtras.provider")
}

version = CurrentVersions.iconv.version

binariesProvider {
}

fun KonanTarget.iconvSrcDir(project: Project): java.io.File =
  project.buildDir.resolve("iconv/$platformName/$version")


fun KonanTarget.iconvPrefix(project: Project): java.io.File =
  project.rootProject.file("libs/iconv/$platformName")

val KonanTarget.iconvNotBuilt: Boolean
  get() = !iconvPrefix(project).resolve("lib/libiconv.la").exists()

val downloadTask = project.createDownloadTask("downloadSrcTask",CurrentVersions.iconv.url,buildDir.resolve("iconv"))

fun srcPrepareFromDownload(target: KonanTarget): Copy =
  tasks.create<Copy>("srcPrepareFromDownload${target.platformName.capitalize()}") {
    dependsOn(downloadTask)
    val srcDir = target.iconvSrcDir(project)

    from(tarTree(resources.gzip(downloadTask.get().outputs.files.first()))) {
      eachFile {
        path = path.substring(relativePath.segments[0].length)
      }
    }
    into(srcDir)
    onlyIf { target.iconvNotBuilt }
  }


fun configureTask(target: KonanTarget) =
  tasks.register("srcConfigure${target.platformName.capitalize()}", Exec::class) {
    //dependsOn("srcPrepare${target.platformName.capitalize()}")
    dependsOn("srcPrepareFromDownload${target.platformName.capitalize()}")


    //to ensure the konan tools are available
    dependsOn(target.konanDepsTaskName)


    val srcDir = target.iconvSrcDir(project)
    workingDir(srcDir)
    environment(target.buildEnvironment())

    val makefile = srcDir.resolve("Makefile")
    outputs.file(makefile)
    onlyIf {
      target.iconvNotBuilt
    }

    val args = listOf(
      "./configure", "-C",
      "--enable-static",
      "--host=${target.hostTriplet}",
      "--prefix=${target.iconvPrefix(project)}",
    )
    commandLine(args)
  }


fun buildTask(target: KonanTarget) =
  tasks.register("build${target.platformName.capitalize()}", Exec::class) {
    dependsOn("srcConfigure${target.platformName.capitalize()}")
    val srcDir = target.iconvSrcDir(project)
    val prefixDir = target.iconvPrefix(project)

    doFirst {
      println("building : $target")
    }
    outputs.file(prefixDir.resolve("lib/libiconv.a"))

    onlyIf {
      target.iconvNotBuilt
    }
    workingDir(srcDir)
    environment(target.buildEnvironment())

    commandLine("make", "install")
    doLast {
      if (didWork)
        srcDir.deleteRecursively()
    }
  }


kotlin {

  declareNativeTargets()

  val commonMain by sourceSets.getting {
    dependencies {
      api(project(":common"))
      implementation(libs.klog)
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  val buildAll by tasks.creating


  targets.withType(KotlinNativeTarget::class).all {

    if (HostManager.hostIsMac == konanTarget.family.isAppleFamily) {
      //srcPrepare(konanTarget)
      srcPrepareFromDownload(konanTarget)
      configureTask(konanTarget)
      buildTask(konanTarget).also {
        buildAll.dependsOn(it)
      }
    }

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
      cinterops.create("libiconv") {
        packageName("libiconv")
        defFile("src/libiconv.def")
      }
    }
  }


}


generateInterops("iconv",file("src/libiconv_header.def"),file("src/libiconv.def"))

