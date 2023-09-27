package org.danbrough.xtras.mqtt

import org.danbrough.xtras.XtrasDSLMarker
import org.danbrough.xtras.env.cygpath
import org.danbrough.xtras.library.XtrasLibrary
import org.danbrough.xtras.library.xtrasCreateLibrary
import org.danbrough.xtras.library.xtrasRegisterSourceTask
import org.danbrough.xtras.log
import org.danbrough.xtras.source.gitSource
import org.gradle.api.Plugin
import org.gradle.api.Project


object Mqtt {
  const val extensionName = "mqtt"
  const val sourceURL = "https://github.com/eclipse/paho.mqtt.c.git"
  const val version = "1.3.12"
  const val commit = "v1.3.12"
}

class MQTTPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.log("MQTTPlugin.apply()")
  }
}


@XtrasDSLMarker
fun Project.xtrasMQTT(
  ssl: XtrasLibrary,
  name: String = Mqtt.extensionName,
  version: String = properties.getOrDefault("mqtt.version", Mqtt.version).toString(),
  commit: String = properties.getOrDefault("mqtt.commit", Mqtt.commit).toString(),
  configure: XtrasLibrary.() -> Unit = {},
) = xtrasCreateLibrary(name, version, ssl) {

  gitSource(Mqtt.sourceURL, commit)

//  cinterops {
//    headers = """
//      #staticLibraries =  libcrypto.a libssl.a
//      headers = openssl/ssl.h openssl/err.h openssl/bio.h openssl/evp.h
//      linkerOpts.linux = -ldl -lc -lm -lssl -lcrypto
//      linkerOpts.android = -ldl -lc -lm -lssl -lcrypto
//      linkerOpts.macos = -ldl -lc -lm -lssl -lcrypto
//      linkerOpts.mingw = -lm -lssl -lcrypto
//      compilerOpts.android = -D__ANDROID_API__=21
//      compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
//      #compilerOpts = -static
//
//          """.trimIndent()
//  }

  configure()
  supportedTargets.forEach { target ->
    val compileDir = sourcesDir(target).resolve("build")
    val configureTask = xtrasRegisterSourceTask(XtrasLibrary.TaskName.CONFIGURE, target) {
      dependsOn(libraryDeps.map { it.extractArchiveTaskName(target) })
      doFirst {
        if (compileDir.exists()) {
          compileDir.deleteRecursively()
        }
        compileDir.mkdirs()
      }
      workingDir(compileDir)
      outputs.file(compileDir.resolve("Makefile"))

      val cmakeArgs = listOf(
        buildEnvironment.binaries.cmake,
        "-G", "Unix Makefiles",
        "-DCMAKE_INSTALL_PREFIX=${buildDir(target).cygpath(buildEnvironment)}",
        "-DPAHO_WITH_SSL=TRUE",
        "-DPAHO_BUILD_STATIC=TRUE",
        "-DPAHO_BUILD_SAMPLES=TRUE",
        "-DOPENSSL_ROOT_DIR=${ssl.libsDir(target).cygpath(buildEnvironment)}",
        ".."
      )

      commandLine(cmakeArgs)
    }

    xtrasRegisterSourceTask(XtrasLibrary.TaskName.BUILD, target) {
      dependsOn(configureTask)
      workingDir(compileDir)
      commandLine(buildEnvironment.binaries.make, "install")
    }
  }
///*  supportedTargets.forEach { target ->
//
//    *//*
//        val configureTask = xtrasRegisterSourceTask(XtrasLibrary.TaskName.CONFIGURE, target) {
//      dependsOn(libraryDeps.map { it.extractArchiveTaskName(target) })
//      dependsOn(prepareSourceTask)
//      outputs.file(workingDir.resolve("Makefile"))
//     *//*
//    val configureTask = xtrasRegisterSourceTask(XtrasLibrary.TaskName.CONFIGURE, target) {
//      dependsOn(libraryDeps.map { it.extractArchiveTaskName(target) })
//      outputs.file(workingDir.resolve("Makefile"))
//      val args = mutableListOf(
//        "./Configure",
//        target.opensslPlatform,
//        "no-tests",
//        "threads",
//        "--prefix=${buildDir(target).absolutePath.replace('\\', '/')}",
//        "--libdir=lib",
//      )
//
//      if (target.family == Family.ANDROID) args += "-D__ANDROID_API__=21"
//      *//*      else if (target.family == Family.MINGW) args += "--cross-compile-prefix=${target.hostTriplet}-"
//            environment("CFLAGS", "  -Wno-macro-redefined ")*//*
//
//      if (HostManager.hostIsMingw) commandLine(
//        buildEnvironment.binaries.bash, "-c", args.joinToString(" ")
//      )
//      else commandLine(args)
//    }
//
//    xtrasRegisterSourceTask(XtrasLibrary.TaskName.BUILD, target) {
//      doFirst {
//        project.log("running make install with CC=${environment["CC"]}")
//      }
//      dependsOn(configureTask)
//      outputs.dir(buildDir(target))
//      commandLine("make", "install_sw")
//      //"make install" requires pod2man which is in /usr/bin/core_perl on archlinux
//      //environment("PATH","/usr/bin/core_perl:${environment["PATH"]}")
//
//    }
//  }*/
}

