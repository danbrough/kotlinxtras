
import BuildEnvironment.buildEnvironment
import BuildEnvironment.declareNativeTargets
import BuildEnvironment.hostTriplet
import BuildEnvironment.konanDepsTaskName
import BuildEnvironment.platformName
import OpenSSL.opensslPlatform
import OpenSSL.opensslPrefix
import OpenSSL.opensslSrcDir
import org.danbrough.kotlinxtras.binaries.CurrentVersions
import org.danbrough.kotlinxtras.sonatype.generateInterops
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget


plugins {
  kotlin("multiplatform")
  `maven-publish`
  id("org.danbrough.kotlinxtras.provider")
}

version = CurrentVersions.openssl

binariesProvider {
  //extra configuration values can go here
}

val KonanTarget.openSSLNotBuilt: Boolean
  get() = !opensslPrefix(project).resolve("include/openssl/ssl.h").exists()


val opensslGitDir = rootProject.file("repos/openssl")

generateInterops("openssl",file("src/openssl_header.def"),file("src/openssl.def"))


fun srcPrepare(target: KonanTarget) =
  tasks.register("srcPrepare${target.platformName.capitalize()}", Exec::class) {
    val srcDir = target.opensslSrcDir(project)
    outputs.dir(srcDir)
    onlyIf { target.openSSLNotBuilt }
    commandLine(BuildEnvironment.gitBinary, "clone", opensslGitDir, srcDir)
  }


fun configureTask(target: KonanTarget) =
  tasks.register("srcConfigure${target.platformName.capitalize()}", Exec::class) {
    dependsOn("srcPrepare${target.platformName.capitalized()}")
    val srcDir = target.opensslSrcDir(project)
    workingDir(srcDir)
    environment(target.buildEnvironment())
    val makeFile = srcDir.resolve("Makefile")
    onlyIf { target.openSSLNotBuilt }
    outputs.file(makeFile)
    doFirst {
      println("OpenSSL Configure $target ..")
    }
    val args = mutableListOf(
      "./Configure", target.opensslPlatform,
      "no-tests", "threads",
      "--prefix=${target.opensslPrefix(project)}",
      //"no-tests","no-ui-console", "--prefix=${target.opensslPrefix(project)}"
    )
    if (target.family == org.jetbrains.kotlin.konan.target.Family.ANDROID)
      args += "-D__ANDROID_API__=${BuildEnvironment.androidNdkApiVersion} "
    else if (target.family == org.jetbrains.kotlin.konan.target.Family.MINGW)
      args += "--cross-compile-prefix=${target.hostTriplet}-"
    commandLine(args)
  }


fun buildTask(target: KonanTarget) =
  tasks.register("build${target.platformName.capitalized()}", Exec::class) {
    dependsOn("srcConfigure${target.platformName.capitalized()}")


    val installDir = target.opensslPrefix(project)

    dependsOn(target.konanDepsTaskName)

    workingDir(target.opensslSrcDir(project))

    val outputFile = installDir.resolve("include/openssl/ssl.h")
    outputs.file(outputFile)
    onlyIf { target.openSSLNotBuilt }

    environment(target.buildEnvironment())
    group = BasePlugin.BUILD_GROUP
    commandLine("make", "install_sw")
    doLast {
      if (didWork)
      target.opensslSrcDir(project).deleteRecursively()
    }
  }


kotlin {
  declareNativeTargets()


  val nativeTest by sourceSets.creating
  val nativeMain by sourceSets.creating

  targets.withType(KotlinNativeTarget::class).all {


    if (BuildEnvironment.hostIsMac == konanTarget.family.isAppleFamily) {
      srcPrepare(konanTarget)
      configureTask(konanTarget)
      buildTask(konanTarget)
    }

    compilations["main"].apply {

      cinterops.create("libopenssl") {
        packageName("libopenssl")
        defFile("src/openssl.def")
      }

      defaultSourceSet.dependsOn(nativeMain)
    }

    compilations["test"].apply {
      defaultSourceSet.dependsOn(nativeTest)
    }
  }
}




