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
import org.jetbrains.kotlin.konan.target.Family


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
}

