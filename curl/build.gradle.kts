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

val curlGitDir = project.file("src/curl.git")

val srcClone by tasks.registering(Exec::class) {
  commandLine(
    BuildEnvironment.gitBinary,
    "clone",
    "--bare",
    "https://github.com/curl/curl.git",
    curlGitDir
  )
  outputs.dir(curlGitDir)
  onlyIf {
    !curlGitDir.exists()
  }
}

fun srcPrepare(target: KonanTarget): Exec =
  tasks.create("srcPrepare${target.platformName.capitalize()}", Exec::class) {
    val srcDir = target.curlSrcDir(project)
    dependsOn(srcClone)
    outputs.dir(srcDir)
    commandLine(
      BuildEnvironment.gitBinary, "clone", "--branch", Curl.TAG, curlGitDir, srcDir
    )
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
    doFirst {
      print("RUNNING AUTOCONF")
    }
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
    dependsOn(srcConfigure)
    val srcDir = target.curlSrcDir(project)
    workingDir(srcDir)
    environment(target.buildEnvironment())
    doFirst {
      println("building : $target")
    }
    
    val curlPrefixDir = target.curlPrefix(project)
    outputs.dir(curlPrefixDir)
    
    val args = listOf(
      "make","install"
    )
    commandLine(args)
  }
}

kotlin {

  declareNativeTargets()
  
  val commonMain by sourceSets.getting {
    dependencies {
      implementation(Dependencies.klog)
    }
  }
  
  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }
  
  targets.withType(KotlinNativeTarget::class).all {
    
    buildTask(konanTarget)
    
    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }
  }
  
  
}