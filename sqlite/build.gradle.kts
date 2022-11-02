

import org.danbrough.kotlinxtras.BuildEnvironment
import org.danbrough.kotlinxtras.BuildEnvironment.buildEnvironment
import org.danbrough.kotlinxtras.BuildEnvironment.declareNativeTargets
import org.danbrough.kotlinxtras.BuildEnvironment.hostTriplet
import org.danbrough.kotlinxtras.binaries.CurrentVersions
import org.danbrough.kotlinxtras.hostIsMac
import org.danbrough.kotlinxtras.konanDepsTaskName
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.sonatype.generateInterops
import org.jetbrains.kotlin.de.undercouch.gradle.tasks.download.Download
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform")
  `maven-publish`
  id("org.danbrough.kotlinxtras.provider")
}

version = CurrentVersions.sqlite
val sqliteGitDir = rootProject.file("repos/sqlite")
val sqliteSource = "https://www.sqlite.org/2022/sqlite-autoconf-3390400.tar.gz"

binariesProvider {

}

fun KonanTarget.sqliteSrcDir(project: Project): java.io.File =
  project.buildDir.resolve("sqlite/$platformName/$version")


fun KonanTarget.sqlitePrefix(project: Project): java.io.File =
  project.rootProject.file("libs/sqlite/$platformName")

val KonanTarget.sqliteNotBuilt: Boolean
  get() = !sqlitePrefix(project).resolve("include/sqlite3.h").exists()

fun srcPrepare(target: KonanTarget): Exec =
  tasks.create("srcPrepare${target.platformName.capitalize()}", Exec::class) {
    val srcDir = target.sqliteSrcDir(project)
    outputs.dir(srcDir)
    commandLine(
      BuildEnvironment.gitBinary, "clone", sqliteGitDir, srcDir
    )
    onlyIf { target.sqliteNotBuilt && !srcDir.exists() }
  }


val downloadSrcTask by tasks.creating(Download::class.java) {
  src(sqliteSource)
  dest(buildDir.resolve("sqlite").also {
    if (!it.exists()) it.mkdirs()
  })
  overwrite(false)
}

fun srcPrepareFromDownload(target: KonanTarget): Copy =
  tasks.create<Copy>("srcPrepareFromDownload${target.platformName.capitalize()}") {
    dependsOn(downloadSrcTask)
    val srcDir = target.sqliteSrcDir(project)
    from(tarTree(resources.gzip(downloadSrcTask.outputFiles.first()))) {
      eachFile {
        path = path.substring(relativePath.segments[0].length)
      }
    }
    into(srcDir)
    onlyIf { target.sqliteNotBuilt }
  }


fun configureTask(target: KonanTarget) =
  tasks.register("srcConfigure${target.platformName.capitalize()}", Exec::class) {
    dependsOn("srcPrepareFromDownload${target.platformName.capitalize()}")


    //to ensure the konan tools are available
    dependsOn(target.konanDepsTaskName)


    val srcDir = target.sqliteSrcDir(project)
    workingDir(srcDir)
    environment(target.buildEnvironment())

    val makefile = srcDir.resolve("Makefile")
    outputs.file(makefile)
    onlyIf {
      target.sqliteNotBuilt
    }

    val args = listOf(
      "./configure",
      "--host=${target.hostTriplet}",
      "--disable-tcl", "--disable-readline",
      "--prefix=${target.sqlitePrefix(project)}",

      )
    commandLine(args)
  }


fun buildTask(target: KonanTarget) =
  tasks.register("build${target.platformName.capitalize()}", Exec::class) {
    dependsOn("srcConfigure${target.platformName.capitalize()}")
    val srcDir = target.sqliteSrcDir(project)
    val sqlitePrefixDir = target.sqlitePrefix(project)

    doFirst {
      println("building : $target")
    }
    outputs.file(sqlitePrefixDir.resolve("include/sqlite3.h"))

    onlyIf {
      target.sqliteNotBuilt
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
      implementation(libs.klog)
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  val buildAll by tasks.creating


  targets.withType(KotlinNativeTarget::class).all {

    if (hostIsMac == konanTarget.family.isAppleFamily) {
      //srcPrepare(konanTarget)
      srcPrepareFromDownload(konanTarget)
      configureTask(konanTarget)
      buildTask(konanTarget).also {
        buildAll.dependsOn(it)
      }
    }

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
      cinterops.create("sqlite") {
        packageName("libsqlite")
        defFile("src/libsqlite.def")
      }
    }
  }


}



generateInterops("sqlite",file("src/libsqlite_header.def"),file("src/libsqlite.def"))


