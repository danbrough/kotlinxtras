import BuildEnvironment.buildEnvironment
import BuildEnvironment.hostTriplet
import BuildEnvironment.platformName
import BuildEnvironment.registerTarget
import BuildEnvironment.konanDepsTask
import BuildEnvironment.declareNativeTargets

import Curl.curlPrefix
import OpenSSL.opensslPlatform
import OpenSSL.opensslPrefix
import OpenSSL.opensslSrcDir
import Curl.curlSrcDir
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.konan.target.Family

plugins {
  kotlin("multiplatform")
  `maven-publish`
}

val curlGitDir = rootProject.file("repos/curl")


fun srcPrepare(target: KonanTarget): Exec =
  tasks.create("srcPrepare${target.platformName.capitalize()}", Exec::class) {
    val srcDir = target.curlSrcDir(project)
    outputs.dir(srcDir)
    commandLine(
      BuildEnvironment.gitBinary, "clone", curlGitDir, srcDir
    )
    onlyIf {
      !srcDir.exists()
    }
  }


fun autoconfTask(target: KonanTarget): Exec {

  val srcPrepareTask = srcPrepare(target)

  return tasks.create("srcAutoconf${target.platformName.capitalize()}", Exec::class) {
    dependsOn(srcPrepareTask)
    val srcDir = target.curlSrcDir(project)
    workingDir(srcDir)
    environment(target.buildEnvironment())
    commandLine("autoreconf", "-fi")
    val configureFile = srcDir.resolve("configure")
    outputs.file(configureFile)
    onlyIf {
      !configureFile.exists()
    }
  }
}

fun configureTask(target: KonanTarget): Exec {

  val srcAutoconf = autoconfTask(target)

  return tasks.create("configure${target.platformName.capitalize()}", Exec::class) {
    dependsOn(srcAutoconf)

    //to ensure the konan tools are available
    dependsOn(target.konanDepsTask(project))
    dependsOn(
      rootProject.project("openssl")
        .getTasksByName("build${target.platformName.capitalized()}", false).first()
    )

    val srcDir = target.curlSrcDir(project)
    workingDir(srcDir)
    environment(target.buildEnvironment())
    doFirst {
      println("ENVIRONMENT: ${environment}")
      println("RUNNING $commandLine")
    }
    //dependsOn("openssl:build${target.platformNameCapitalized}")

    val makefile = srcDir.resolve("Makefile")
    outputs.file(makefile)
    onlyIf {
      !makefile.exists()
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
}


fun buildTask(target: KonanTarget): Exec {

  val srcConfigure = configureTask(target)

  return tasks.create("build${target.platformName.capitalize()}", Exec::class) {


    val srcDir = target.curlSrcDir(project)

    val curlPrefixDir = target.curlPrefix(project)


      dependsOn(srcConfigure)


    workingDir(srcDir)
    environment(target.buildEnvironment())
    doFirst {
      println("building : $target")
    }

    outputs.dir(curlPrefixDir)

    val args = listOf(
      "make", "install"
    )
    commandLine(args)
  }
}

kotlin {

  declareNativeTargets()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(project(":openssl"))
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  val buildAll by tasks.creating


  targets.withType(KotlinNativeTarget::class).all {

    if (BuildEnvironment.hostIsMac == konanTarget.family.isAppleFamily) {
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


val generateCInteropsDef by tasks.creating {
  inputs.file("src/libcurl_header.def")
  outputs.file("src/libcurl.def")
  doFirst {
    val outputFile = outputs.files.files.first()
    println("Generating $outputFile")
    outputFile.printWriter().use { output ->
      output.println(inputs.files.files.first().readText())
      kotlin.targets.withType<KotlinNativeTarget>().forEach {
        val konanTarget = it.konanTarget
        output.println("compilerOpts.${konanTarget.name} = -Ibuild/libs/curl/${konanTarget.platformName}/include \\")
        output.println("\t-I/usr/local/kotlinxtras/libs/curl/${konanTarget.platformName}/include ")
        output.println("linkerOpts.${konanTarget.name} = -Lbuild/libs/curl/${konanTarget.platformName}/lib \\")
        output.println("\t-L/usr/local/kotlinxtras/libs/curl/${konanTarget.platformName}/lib ")
        output.println("libraryPaths.${konanTarget.name} = build/libs/curl/${konanTarget.platformName}/lib \\")
        output.println("\t/usr/local/kotlinxtras/libs/curl/${konanTarget.platformName}/lib ")
      }
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.CInteropProcess>() {
  dependsOn(generateCInteropsDef)
}