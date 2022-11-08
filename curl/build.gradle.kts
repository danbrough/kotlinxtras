


import org.danbrough.kotlinxtras.BuildEnvironment.buildEnvironment
import org.danbrough.kotlinxtras.BuildEnvironment.declareNativeTargets
import org.danbrough.kotlinxtras.BuildEnvironment.hostTriplet
import org.danbrough.kotlinxtras.OpenSSL.opensslPrefix
import org.danbrough.kotlinxtras.binaries.CurrentVersions
import org.danbrough.kotlinxtras.konanDepsTaskName
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.sonatype.generateInterops
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform")
  `maven-publish`
  id("org.danbrough.kotlinxtras.provider")
}

repositories {

}

version = CurrentVersions.curl



binariesProvider {
 //for extra configuration

}

fun KonanTarget.curlSrcDir(project: Project): File =
  project.buildDir.resolve("curl/$platformName")


fun KonanTarget.curlPrefix(project: Project): File =project.rootProject.file("libs/curl/$platformName")

val curlGitDir = rootProject.file("repos/curl")

val KonanTarget.curlBuilt: Boolean
  get() = curlPrefix(project).resolve("include/curl/curl.h").exists()

fun srcPrepare(target: KonanTarget): Exec =
  tasks.create("srcPrepare${target.platformName.capitalize()}", Exec::class) {
    val srcDir = target.curlSrcDir(project)

    outputs.dir(srcDir)
    commandLine(org.danbrough.kotlinxtras.BuildEnvironment.gitBinary, "clone", curlGitDir, srcDir)
    onlyIf { !target.curlBuilt && !srcDir.exists() }

  }


fun autoconfTask(target: KonanTarget) =
  tasks.register("srcAutoconf${target.platformName.capitalize()}", Exec::class) {
    dependsOn("srcPrepare${target.platformName.capitalize()}")
    val srcDir = target.curlSrcDir(project)
    workingDir(srcDir)
    environment(target.buildEnvironment())
    commandLine("autoreconf", "-fi")
    val configureFile = srcDir.resolve("configure")
    outputs.file(configureFile)
    onlyIf {
      !target.curlBuilt
    }
  }


fun configureTask(target: KonanTarget) =
  tasks.register("srcConfigure${target.platformName.capitalize()}", Exec::class) {
    dependsOn("srcAutoconf${target.platformName.capitalize()}")


    //to ensure the konan tools are available
    dependsOn(target.konanDepsTaskName)
    dependsOn(":openssl:build${target.platformName.capitalized()}")

    val srcDir = target.curlSrcDir(project)
    workingDir(srcDir)
    environment(target.buildEnvironment())

    val makefile = srcDir.resolve("Makefile")
    outputs.file(makefile)
    onlyIf {
      !target.curlBuilt
    }

    val args = listOf(
      "./configure",
      "--host=${target.hostTriplet}",
      "--with-ssl=${target.opensslPrefix(project)}",
      "--with-ca-path=/etc/ssl/certs:/etc/security/cacerts:/etc/ca-certificates",
      "--prefix=${target.curlPrefix(project)}",
      )
    commandLine(args)
  }


fun buildTask(target: KonanTarget) =
  tasks.register("build${target.platformName.capitalize()}", Exec::class) {
    dependsOn("srcConfigure${target.platformName.capitalize()}")
    val srcDir = target.curlSrcDir(project)
    val curlPrefixDir = target.curlPrefix(project)

    doFirst {
      println("building : $target")
    }
    outputs.file(curlPrefixDir.resolve("include/curl/curl.h"))

    onlyIf {
      !target.curlBuilt
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
      api(project(":openssl"))
    }
  }

  val commonTest by sourceSets.getting {
    dependencies {
      implementation(kotlin("test"))
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  val posixTest by sourceSets.creating {
    dependsOn(commonTest)
  }

  val buildAll by tasks.creating


  targets.withType(KotlinNativeTarget::class).all {

    if (HostManager.hostIsMac == konanTarget.family.isAppleFamily) {
      srcPrepare(konanTarget)
      autoconfTask(konanTarget)
      configureTask(konanTarget)
      buildTask(konanTarget).also {
        buildAll.dependsOn(it)
      }
    }

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
      cinterops.create("curl") {
        packageName("libcurl")
        defFile("src/libcurl.def")
      }
    }
  }
}



generateInterops("curl",file("src/libcurl_header.def"),file("src/libcurl.def"))
