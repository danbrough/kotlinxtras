import BuildEnvironment.buildEnvironment
import BuildEnvironment.declareNativeTargets
import BuildEnvironment.hostTriplet
import BuildEnvironment.konanDepsTask
import BuildEnvironment.platformName
import Curl.curlPrefix
import Curl.curlSrcDir
import KotlinXtras_gradle.KotlinXtras.configureBinarySupport
import OpenSSL.opensslPrefix
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform")
  `maven-publish`
}

version = "curl-7_85_0"

val curlGitDir = rootProject.file("repos/curl")


val KonanTarget.curlNotBuilt: Boolean
  get() = !curlPrefix(project).resolve("include/curl/curl.h").exists()

fun srcPrepare(target: KonanTarget): Exec =
  tasks.create("srcPrepare${target.platformName.capitalize()}", Exec::class) {
    val srcDir = target.curlSrcDir(project)
    outputs.dir(srcDir)
    commandLine(
      BuildEnvironment.gitBinary, "clone", curlGitDir, srcDir
    )
    onlyIf { target.curlNotBuilt && !srcDir.exists()}

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
       target.curlNotBuilt
    }
  }


fun configureTask(target: KonanTarget) =
  tasks.register("srcConfigure${target.platformName.capitalize()}", Exec::class) {
    dependsOn("srcAutoconf${target.platformName.capitalize()}")


    //to ensure the konan tools are available
    dependsOn(target.konanDepsTask(project))
    dependsOn(":openssl:build${target.platformName.capitalized()}")

    val srcDir = target.curlSrcDir(project)
    workingDir(srcDir)
    environment(target.buildEnvironment())

    val makefile = srcDir.resolve("Makefile")
    outputs.file(makefile)
    onlyIf {
      target.curlNotBuilt
    }

    val args = listOf(
      "./configure",
      "--host=${target.hostTriplet}",
      "--with-ssl=${target.opensslPrefix(project)}",
      "--with-ca-path=/data/cacerts:/etc/security/cacerts:/etc/ca-certificates:/etc/ssl/certs",
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
      target.curlNotBuilt
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

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  val buildAll by tasks.creating


  targets.withType(KotlinNativeTarget::class).all {

    if (BuildEnvironment.hostIsMac == konanTarget.family.isAppleFamily) {
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


tasks.register("generateCInteropsDef") {
  inputs.file("src/libcurl_header.def")
  outputs.file("src/libcurl.def")
  doFirst {
    val outputFile = outputs.files.files.first()
    println("Generating $outputFile")
    outputFile.printWriter().use { output ->
      output.println(inputs.files.files.first().readText())
      kotlin.targets.withType<KotlinNativeTarget>().forEach {
        val konanTarget = it.konanTarget
        output.println("compilerOpts.${konanTarget.name} = -Ibuild/kotlinxtras/curl/${konanTarget.platformName}/include \\")
        output.println("\t-I/usr/local/kotlinxtras/libs/curl/${konanTarget.platformName}/include ")
        output.println("linkerOpts.${konanTarget.name} = -Lbuild/kotlinxtras/curl/${konanTarget.platformName}/lib \\")
        output.println("\t-L/usr/local/kotlinxtras/libs/curl/${konanTarget.platformName}/lib ")
        output.println("libraryPaths.${konanTarget.name} = build/kotlinxtras/curl/${konanTarget.platformName}/lib \\")
        output.println("\t/usr/local/kotlinxtras/libs/curl/${konanTarget.platformName}/lib ")
      }
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.CInteropProcess>() {
  dependsOn("generateCInteropsDef")
  if (BuildEnvironment.hostIsMac == konanTarget.family.isAppleFamily)
    dependsOn("build${konanTarget.platformName.capitalized()}")
}

project.configureBinarySupport()
