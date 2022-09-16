import BuildEnvironment.buildEnvironment
import BuildEnvironment.konanDepsTask
import BuildEnvironment.hostTriplet
import BuildEnvironment.platformName
import OpenSSL.opensslPlatform
import OpenSSL.opensslPrefix
import OpenSSL.opensslSrcDir
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import BuildEnvironment.declareNativeTargets
import KotlinXtras_gradle.KotlinXtras.configureBinarySupport
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.konan.target.KonanTarget


plugins {
  kotlin("multiplatform")
  `maven-publish`
}

version = "OpenSSL_1_1_1q"


val KonanTarget.openSSLNotBuilt: Boolean
  get() = !opensslPrefix(project).resolve("include/openssl/ssl.h").exists()


val opensslGitDir = rootProject.file("repos/openssl")

tasks.register("generateCInteropsDef") {
  inputs.file("src/openssl_header.def")
  outputs.file("src/openssl.def")
  doFirst {
    val outputFile = outputs.files.files.first()
    println("Generating $outputFile")

    outputFile.printWriter().use { output ->
      output.println(inputs.files.files.first().readText())
      kotlin.targets.withType<KotlinNativeTarget>().forEach {
        val konanTarget = it.konanTarget
        output.println("compilerOpts.${konanTarget.name} = -Ibuild/kotlinxtras/openssl/${konanTarget.platformName}/include \\")
        output.println("\t-I/usr/local/kotlinxtras/libs/openssl/${konanTarget.platformName}/include ")
        output.println("linkerOpts.${konanTarget.name} = -Lbuild/kotlinxtras/openssl/${konanTarget.platformName}/lib \\")
        output.println("\t-L/usr/local/kotlinxtras/libs/openssl/${konanTarget.platformName}/lib ")
      }
    }
  }
}

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

    dependsOn(target.konanDepsTask(project))

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

tasks.withType<CInteropProcess>() {
  dependsOn("generateCInteropsDef")
}

project.configureBinarySupport()