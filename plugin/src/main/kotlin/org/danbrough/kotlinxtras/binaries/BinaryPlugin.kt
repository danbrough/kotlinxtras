package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.PROPERTY_CINTEROPS_DIR
import org.danbrough.kotlinxtras.PROPERTY_DOCS_DIR
import org.danbrough.kotlinxtras.PROPERTY_DOWNLOADS_DIR
import org.danbrough.kotlinxtras.PROPERTY_LIBS_DIR
import org.danbrough.kotlinxtras.PROPERTY_PACKAGES_DIR
import org.danbrough.kotlinxtras.PROPERTY_XTRAS_DIR
import org.danbrough.kotlinxtras.SHARED_LIBRARY_PATH_NAME
import org.danbrough.kotlinxtras.XTRAS_BINARY_PLUGIN_ID
import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.goArch
import org.danbrough.kotlinxtras.goOS
import org.danbrough.kotlinxtras.hostTriplet
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.projectProperty
import org.danbrough.kotlinxtras.sharedLibraryPath
import org.danbrough.kotlinxtras.xtrasCInteropsDir
import org.danbrough.kotlinxtras.xtrasDir
import org.danbrough.kotlinxtras.xtrasDocsDir
import org.danbrough.kotlinxtras.xtrasDownloadsDir
import org.danbrough.kotlinxtras.xtrasLibsDir
import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

private val buildCacheDir = File("/tmp/buildCache")

open class BinaryExtension {
  @XtrasDSLMarker
  var gitBinary: String = "/usr/bin/git"

  @XtrasDSLMarker
  var wgetBinary: String = "/usr/bin/wget"

  @XtrasDSLMarker
  var tarBinary: String = "/usr/bin/tar"

  @XtrasDSLMarker
  var autoreconfBinary: String = "/usr/bin/autoreconf"

  @XtrasDSLMarker
  var makeBinary: String = "/usr/bin/make"

  @XtrasDSLMarker
  var cmakeBinary: String = "/usr/bin/cmake"

  @XtrasDSLMarker
  var goBinary: String = "/usr/bin/go"

  var libraryExtensions = mutableListOf<LibraryExtension>()


  @XtrasDSLMarker
  var konanDir = File("${System.getProperty("user.home")}/.konan")

  @XtrasDSLMarker
  var androidNdkApiVersion = 21

  @XtrasDSLMarker
  var androidNdkDir = konanDir.resolve(
    if (HostManager.hostIsMac) "dependencies/target-toolchain-2-osx-android_ndk" else "dependencies/target-toolchain-2-linux-android_ndk"
  )

  private val envs: MutableMap<KonanTarget, MutableMap<String, Any?>?> = mutableMapOf()


  @XtrasDSLMarker
  var basePath = mutableListOf(
    "/bin", "/sbin", "/usr/bin", "/usr/sbin", "/usr/local/bin", "/opt/local/bin"
  )


  fun environment(target: KonanTarget): MutableMap<String, Any?> {
    return envs[target] ?: mutableMapOf<String, Any?>().also {
      envs[target] = it
      configureEnv(target, it)
    }
  }

  protected open fun configureEnv(target: KonanTarget, env: MutableMap<String, Any?>) {
    env["KONAN_BUILD"] = 1

    env["ANDROID_NDK_HOME"] = androidNdkDir.absolutePath
    env["ANDROID_NDK_ROOT"] = androidNdkDir.absolutePath


    when (target) {

      KonanTarget.LINUX_ARM32_HFP -> {
        val clangArgs =
          "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2/arm-unknown-linux-gnueabihf/sysroot "
        env["CC"] = "clang $clangArgs"
        env["CXX"] = "clang++ $clangArgs"
      }

      KonanTarget.LINUX_ARM64 -> {
        val clangArgs =
          "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2 --sysroot=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/aarch64-unknown-linux-gnu/sysroot"
        env["CC"] = "clang $clangArgs"
        env["CXX"] = "clang++ $clangArgs"
      }

      KonanTarget.LINUX_X64 -> {
        val clangArgs =
          "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot"
        env["CC"] = "clang $clangArgs"
        env["CXX"] = "clang++ $clangArgs"/*        env["RANLIB"] =
                  "$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/bin/ranlib"*/
      }

      KonanTarget.MACOS_X64, KonanTarget.MACOS_ARM64 -> {
        env["CC"] = "gcc"
        env["CXX"] = "g++"
        env["LD"] = "lld"
      }


      KonanTarget.MINGW_X64 -> {
        //You need mingw installed for this target
      }

      KonanTarget.ANDROID_X64, KonanTarget.ANDROID_X86, KonanTarget.ANDROID_ARM64, KonanTarget.ANDROID_ARM32 -> {

        basePath.add(0, androidNdkDir.resolve("bin").absolutePath)
        env["CC"] = "${target.hostTriplet}${androidNdkApiVersion}-clang"
        env["CXX"] = "${target.hostTriplet}${androidNdkApiVersion}-clang++"
        env["AR"] = "llvm-ar"
        env["RANLIB"] = "ranlib"
      }

      else -> {
        throw Error("Unsupported target: $target")
      }
    }

    if (HostManager.hostIsMac) basePath.add(
      0, konanDir.resolve("dependencies/apple-llvm-20200714-macos-x64-essentials/bin").absolutePath
    )

    basePath.add(
      0, konanDir.resolve("dependencies/llvm-11.1.0-linux-x64-essentials/bin").absolutePath
    )

    env["PATH"] = basePath.joinToString(File.pathSeparator)
    env["GOARCH"] = target.goArch
    env["GOOS"] = target.goOS
    env["GOARM"] = 7
    env["CGO_CFLAGS"] = env["CFLAGS"]
    env["CGO_LDFLAGS"] = env["LDFLAGS"]
    env["CGO_ENABLED"] = 1
    env["MAKE"] = "make -j4"
    envConfig?.invoke(target, env)
  }

  private var envConfig: ((KonanTarget, MutableMap<String, Any?>) -> Unit)? = null

  @XtrasDSLMarker
  fun initEnvironment(config: (KonanTarget, MutableMap<String, Any?>) -> Unit) {
    envConfig = config
  }


}


const val XTRAS_BINARIES_EXTN_NAME = "xtrasBinaries"


val binaryProperty: Project.(String, String) -> String = { exe, defValue ->
  projectProperty("$binaryPropertyPrefix.$exe", defValue)
}

const val binaryPropertyPrefix = "xtras.bin"


class BinaryPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.log("Initializing BinaryPlugin...")
    target.extensions.create(XTRAS_BINARIES_EXTN_NAME, BinaryExtension::class.java).apply {


      gitBinary = target.binaryProperty("git", gitBinary)
      wgetBinary = target.binaryProperty("wget", wgetBinary)
      goBinary = target.binaryProperty("go", goBinary)
      tarBinary = target.binaryProperty("tar", tarBinary)
      autoreconfBinary = target.binaryProperty("autoreconf", autoreconfBinary)
      makeBinary = target.binaryProperty("make", makeBinary)
      cmakeBinary = target.binaryProperty("cmake", cmakeBinary)


      target.tasks.register("xtrasConfig") {
        group = XTRAS_TASK_GROUP
        description = "Prints out the xtras configuration details"

        doFirst {
          println(
            """
                
                Binaries:
                  $binaryPropertyPrefix.git:            $gitBinary
                  $binaryPropertyPrefix.wget:           $wgetBinary
                  $binaryPropertyPrefix.tar:            $tarBinary
                  $binaryPropertyPrefix.go:             $goBinary
                  $binaryPropertyPrefix.autoreconf:     $autoreconfBinary
                  $binaryPropertyPrefix.make:           $makeBinary
                  $binaryPropertyPrefix.cmake:          $cmakeBinary
                
                Paths:
                  $PROPERTY_XTRAS_DIR:            ${project.xtrasDir}
                  $PROPERTY_LIBS_DIR:       ${project.xtrasLibsDir}
                  $PROPERTY_DOWNLOADS_DIR:  ${project.xtrasDownloadsDir}
                  $PROPERTY_PACKAGES_DIR:   ${project.xtrasPackagesDir}
                  $PROPERTY_DOCS_DIR:       ${project.xtrasDocsDir}
                  $PROPERTY_CINTEROPS_DIR:  ${project.xtrasCInteropsDir}
                """.trimIndent()
          )
        }
      }


      target.afterEvaluate {
        tasks.withType(KotlinJvmTest::class.java) {
          dependsOn(libraryExtensions.map { it.extractArchiveTaskName(HostManager.host) })
          environment(SHARED_LIBRARY_PATH_NAME, sharedLibraryPath())
        }
      }
    }

  }
}


val Project.binariesExtension: BinaryExtension
  get() = project.extensions.findByType(BinaryExtension::class.java) ?: let {
    project.plugins.apply(XTRAS_BINARY_PLUGIN_ID)
    project.extensions.getByType(BinaryExtension::class.java)
  }


/*
fun KonanTarget.buildEnvironment(): MutableMap<String, *> = mutableMapOf(
  "CGO_ENABLED" to 1, "GOARM" to 7, "GOOS" to goOS, "GOARCH" to goArch,
  "GOBIN" to buildCacheDir.resolve("$name/bin"),
  "GOCACHE" to buildCacheDir.resolve("$name/gobuild"),
  "GOCACHEDIR" to buildCacheDir.resolve("$name/gocache"),
  "GOMODCACHE" to buildCacheDir.resolve("gomodcache"),
  "GOPATH" to buildCacheDir.resolve(name),
  "KONAN_DATA_DIR" to konanDir,
  "CFLAGS" to "-O3 -pthread -Wno-macro-redefined -Wno-deprecated-declarations ",//-DOPENSSL_SMALL_FOOTPRINT=1",
  "MAKE" to "make -j4",
).apply {
  val path = buildPathEnvironment.split(':').toMutableList()


}
*/


