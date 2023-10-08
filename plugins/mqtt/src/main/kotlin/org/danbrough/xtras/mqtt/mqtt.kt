package org.danbrough.xtras.mqtt

import org.danbrough.xtras.XtrasDSLMarker
import org.danbrough.xtras.androidLibDir
import org.danbrough.xtras.env.cygpath
import org.danbrough.xtras.library.XtrasLibrary
import org.danbrough.xtras.library.xtrasCreateLibrary
import org.danbrough.xtras.library.xtrasRegisterSourceTask
import org.danbrough.xtras.log
import org.danbrough.xtras.source.gitSource
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.utils.`is`
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget


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

      /*

OPTS="-DPAHO_BUILD_DOCUMENTATION=FALSE -DPAHO_BUILD_SAMPLES=TRUE -DCMAKE_MINIMUM_REQUIRED_VERSION=2.8 \
-DCMAKE_INSTALL_PREFIX=$PREFIX \
-DANDROID_ABI=$ANDROID_ABI \
-DCMAKE_TOOLCHAIN_FILE=$NDK/build/cmake/android.toolchain.cmake -DANDROID_PLATFORM=21 \
-DPAHO_ENABLE_TESTING=FALSE -DPAHO_WITH_SSL=TRUE \
-DOPENSSL_CRYPTO_LIBRARY=$OPENSSL/lib/libcrypto.so \
-DOPENSSL_SSL_LIBRARY=$OPENSSL/lib/libssl.so  \
-DOPENSSL_ROOT_DIR=$OPENSSL  \
 -DOPENSSL_LIBRARIES=$OPENSSL/lib -DOPENSSL_INCLUDE_DIR=$OPENSSL/include "
       */
      val cmakeArgs = mutableListOf(
        buildEnvironment.binaries.cmake,
        "-G", "Unix Makefiles",
        "-DCMAKE_INSTALL_PREFIX=${buildDir(target).cygpath(buildEnvironment)}",
        "-DPAHO_WITH_SSL=TRUE",
        "-DPAHO_BUILD_STATIC=TRUE",
        "-DPAHO_BUILD_SHARED=TRUE",
        "-DPAHO_ENABLE_TESTING=FALSE",
        "-DPAHO_BUILD_SAMPLES=TRUE",
        "-DPAHO_BUILD_DOCUMENTATION=FALSE",
        "-DOPENSSL_ROOT_DIR=${ssl.libsDir(target).cygpath(buildEnvironment)}",
      )

      if (target.family == Family.ANDROID) {
        cmakeArgs += listOf(
          "-DANDROID_ABI=${target.androidLibDir}",
          "-DANDROID_PLATFORM=21",
          "-DCMAKE_TOOLCHAIN_FILE=${
            buildEnvironment.androidNdkDir.resolve("build/cmake/android.toolchain.cmake")
              .cygpath(buildEnvironment)
          }",
          "-DOPENSSL_SSL_LIBRARY=${
            ssl.libsDir(target).resolve("lib/libssl.so").cygpath(buildEnvironment)
          }",
          "-DOPENSSL_CRYPTO_LIBRARY=${
            ssl.libsDir(target).resolve("lib/libcrypto.so").cygpath(buildEnvironment)
          }",
          "-DOPENSSL_INCLUDE_DIR=${
            ssl.libsDir(target).resolve("include").cygpath(buildEnvironment)
          }",
          "-DOPENSSL_LIBRARIES=${
            ssl.libsDir(target).resolve("lib").cygpath(buildEnvironment)
          }",
        )
      } else if (target.family.isAppleFamily) {
        if (target == KonanTarget.MACOS_X64)
          cmakeArgs += "-DCMAKE_OSX_ARCHITECTURES=x86_64"
        else if (target == KonanTarget.MACOS_ARM64)
          cmakeArgs += "-DCMAKE_OSX_ARCHITECTURES=arm64"
      } else if (target.family == Family.MINGW) {
        cmakeArgs += listOf(
          "-DCMAKE_C_COMPILER=x86_64-w64-mingw32-gcc",
          "-DCMAKE_CXX_COMPILER=x86_64-w64-mingw32-g++",
          "-DCMAKE_SYSTEM_NAME=Windows",
          "-DCMAKE_SYSTEM_VERSION=1",
          "-DOPENSSL_CRYPTO_LIBRARY=${
            ssl.libsDir(target).resolve("lib/libcrypto.a").cygpath(buildEnvironment)
          }",
          "-DOPENSSL_SSL_LIBRARY=${
            ssl.libsDir(target).resolve("lib/libssl.a").cygpath(buildEnvironment)
          }",
        )

      }

      /*
      OPTS="-DPAHO_BUILD_DOCUMENTATION=FALSE -DPAHO_BUILD_SAMPLES=TRUE -DCMAKE_MINIMUM_REQUIRED_VERSION=2.8 \
-DCMAKE_INSTALL_PREFIX=$PREFIX \
-DCMAKE_C_COMPILER=x86_64-w64-mingw32-gcc \
-DCMAKE_SYSTEM_NAME=Windows \
-DCMAKE_SYSTEM_VERSION=1 \
-DPAHO_BUILD_STATIC=TRUE \
-DPAHO_BUILD_SHARED=TRUE \
-DPAHO_ENABLE_TESTING=FALSE -DPAHO_WITH_SSL=TRUE \
-DOPENSSL_ROOT_DIR=$OPENSSL  \
-DOPENSSL_SSL_LIBRARY=$OPENSSL/lib/libssl.a \
-DOPENSSL_CRYPTO_LIBRARY=$OPENSSL/lib/libcrypto.a \
 -DOPENSSL_LIBRARIES=$OPENSSL/lib -DOPENSSL_INCLUDE_DIR=$OPENSSL/include "

      # Name of the target platform
SET(CMAKE_SYSTEM_NAME Windows)

# Version of the system
SET(CMAKE_SYSTEM_VERSION 1)

# specify the cross compiler
SET(CMAKE_C_COMPILER x86_64-w64-mingw32-gcc)
SET(CMAKE_CXX_COMPILER x86_64-w64-mingw32-g++)
SET(CMAKE_RC_COMPILER_ENV_VAR "RC")
SET(CMAKE_RC_COMPILER "")
SET(CMAKE_SHARED_LINKER_FLAGS
    "-fdata-sections -ffunction-sections -Wl,--enable-stdcall-fixup -static-libgcc -static -lpthread" CACHE STRING "" FORCE)
SET(CMAKE_EXE_LINKER_FLAGS
    "-fdata-sections -ffunction-sections -Wl,--enable-stdcall-fixup -static-libgcc -static -lpthread" CACHE STRING "" FORCE)


       */
      cmakeArgs += ".."

      commandLine(cmakeArgs)
    }

    xtrasRegisterSourceTask(XtrasLibrary.TaskName.BUILD, target) {
      dependsOn(configureTask)
      workingDir(compileDir)
      commandLine(buildEnvironment.binaries.make, "install")
    }
  }
}

