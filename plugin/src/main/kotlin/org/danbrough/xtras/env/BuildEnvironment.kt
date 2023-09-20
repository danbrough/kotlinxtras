@file:Suppress("MemberVisibilityCanBePrivate")

package org.danbrough.xtras.env


import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasDSLMarker
import org.danbrough.xtras.XtrasPath
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.log
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.xtrasCInteropsDir
import org.danbrough.xtras.xtrasDir
import org.danbrough.xtras.xtrasDocsDir
import org.danbrough.xtras.xtrasDownloadsDir
import org.danbrough.xtras.xtrasLibsDir
import org.danbrough.xtras.xtrasLogsDir
import org.danbrough.xtras.xtrasMavenDir
import org.danbrough.xtras.xtrasNdkDir
import org.danbrough.xtras.xtrasPackagesDir
import org.danbrough.xtras.xtrasSourceDir
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.util.Date


data class Binaries(

  @XtrasDSLMarker var git: String = "git",

  @XtrasDSLMarker var wget: String = "wget",

  @XtrasDSLMarker var tar: String = "tar",

  @XtrasDSLMarker var autoreconf: String = "autoreconf",

  @XtrasDSLMarker var make: String = "make",

  @XtrasDSLMarker var cmake: String = "cmake",

  @XtrasDSLMarker var go: String = "go",

  @XtrasDSLMarker var bash: String = "bash",
)

val binaryProperty: Project.(String, String) -> String = { exe, defValue ->
  projectProperty("$binaryPropertyPrefix.$exe", defValue)
}

const val binaryPropertyPrefix = "xtras.bin"

open class BuildEnvironment : Cloneable {
  companion object {
    val ANDROID_NDK_NOT_SET = File(System.getProperty("java.io.tmpdir"), "android_ndk_not_set")
  }

  var binaries = Binaries()

  lateinit var konanDir: File

  public override fun clone(): Any = BuildEnvironment().also {
    it.binaries = binaries.copy()
    it.konanDir = konanDir
    it.basePath = basePath
    it.androidNdkApiVersion = androidNdkApiVersion
    it.defaultEnvironment = defaultEnvironment
    it.androidNdkDir = androidNdkDir
    it.environmentForTarget = environmentForTarget
  }

  @XtrasDSLMarker
  var basePath: List<String> =
    listOf("/bin", "/sbin", "/usr/bin", "/usr/sbin", "/usr/local/bin", "/opt/local/bin")

  @XtrasDSLMarker
  var androidNdkApiVersion = 21


  /**
   * The java language version to apply to jvm and kotlin-jvm builds.
   * Not applied if null.
   * default: 8
   */
  @XtrasDSLMarker
  var javaLanguageVersion: Int? = 8

  @XtrasDSLMarker
  var defaultEnvironment: Map<String, String> = buildMap {

    if (!HostManager.hostIsMingw) put("PATH", basePath.joinToString(File.pathSeparator))
    else put("BASH_ENV", "/etc/profile")

    put("MAKE", "make -j${Runtime.getRuntime().availableProcessors()}")

    //put("CFLAGS", "-O3 -pthread -Wno-macro-redefined -Wno-deprecated-declarations")

    put("KONAN_BUILD", "1")
  }


  @XtrasDSLMarker
  var androidNdkDir: File = ANDROID_NDK_NOT_SET


  @XtrasDSLMarker
  var environmentForTarget: MutableMap<String, String>.(KonanTarget) -> Unit = { target ->

    if (!HostManager.hostIsMac || !target.family.isAppleFamily) {
      val llvmPrefix = if (HostManager.hostIsLinux) "llvm-" else "apple-llvm"
      konanDir.resolve("dependencies").listFiles()
        ?.firstOrNull { it.isDirectory && it.name.startsWith(llvmPrefix) }?.also {
          put("PATH", "${it.resolve("bin").absolutePath}:${get("PATH")}")
        }
    }

    var clangArgs: String? = null


    when (target) {
      KonanTarget.LINUX_ARM64 -> {
        clangArgs =
          "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2 --sysroot=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/aarch64-unknown-linux-gnu/sysroot"
      }

      KonanTarget.LINUX_X64 -> {
        if (HostManager.hostIsLinux) clangArgs =
          "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot"
      }

      KonanTarget.LINUX_ARM32_HFP -> {
        if (HostManager.hostIsLinux) clangArgs =
          "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2  --sysroot=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2/arm-unknown-linux-gnueabihf/sysroot"
      }

      KonanTarget.MACOS_X64, KonanTarget.MACOS_ARM64, KonanTarget.WATCHOS_X64, KonanTarget.WATCHOS_ARM64, KonanTarget.IOS_X64, KonanTarget.IOS_ARM64 -> {
        put("CC", "clang")
        //put("CXX", "g++")
        //put("LD", "lld")
      }

      KonanTarget.MINGW_X64 -> {
        /*     if (HostManager.hostIsMingw) clangArgs = "--target=${target.hostTriplet} --gcc-toolchain=${
               konanDir.resolve("dependencies/msys2-mingw-w64-x86_64-2").filePath
             }   --sysroot=${konanDir.resolve("dependencies/msys2-mingw-w64-x86_64-2/x86_64-w64-mingw32").filePath}"*/

        //clangArgs = "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2 --sysroot=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/aarch64-unknown-linux-gnu/sysroot"

        //   put("CC", "x86_64-w64-mingw32-gcc")
        //put("AR", "x86_64-w64-mingw32-ar")
        //put("RANLIB", "x86_64-w64-mingw32-ranlib")
      }

      KonanTarget.ANDROID_X64, KonanTarget.ANDROID_X86, KonanTarget.ANDROID_ARM64, KonanTarget.ANDROID_ARM32 -> {
        //library.project.log("ADDING NDK TO PATH")
        val archFolder = when {
          HostManager.hostIsLinux -> "linux-x86_64"
          HostManager.hostIsMac -> "darwin-x86_64"
          HostManager.hostIsMingw -> "windows-x86_64"
          else -> error("Unhandled host: ${HostManager.host}")
        }
        put(
          "PATH",
          "${androidNdkDir.resolve("toolchains/llvm/prebuilt/$archFolder/bin").filePath}${File.pathSeparator}${
            get("PATH")
          }"
        )


        //basePath.add(0, androidNdkDir.resolve("bin").absolutePath)
        put("CC", "${target.hostTriplet}${androidNdkApiVersion}-clang")
        put("CXX", "${target.hostTriplet}${androidNdkApiVersion}-clang++")
        put("AR", "llvm-ar")
        put("RANLIB", "ranlib")

      }

      else -> error("Unhandled target: $target")
    }

    if (clangArgs != null) {
      put("CC", "clang $clangArgs")
      put("CXX", "clang++ $clangArgs")
    }
  }

  fun getEnvironment(target: KonanTarget? = null) = buildMap {
    putAll(defaultEnvironment)
    if (target != null) environmentForTarget(target)
  }

  @XtrasDSLMarker
  fun binaries(config: Binaries.() -> Unit) {
    binaries.config()
  }

  internal fun initialize(project: Project) {

    konanDir = System.getenv("KONAN_DATA_DIR")?.let { File(it) } ?: File(
      System.getProperty("user.home"), ".konan"
    )

    if (androidNdkDir == ANDROID_NDK_NOT_SET) {
      val ndkRoot = System.getenv("ANDROID_NDK_ROOT") ?: System.getenv("ANDROID_NDK_HOME")
      if (ndkRoot != null) androidNdkDir = File(ndkRoot)
      else {
        androidNdkDir = project.xtrasNdkDir

        project.log("Neither ANDROID_NDK_ROOT or ANDROID_NDK_HOME are set!", LogLevel.WARN)
      }
    }

    binaries.apply {
      git = project.binaryProperty("git", git)
      wget = project.binaryProperty("wget", wget)
      go = project.binaryProperty("go", go)
      tar = project.binaryProperty("tar", tar)
      make = project.binaryProperty("make", make)
      cmake = project.binaryProperty("cmake", cmake)
      bash = project.binaryProperty("bash", bash)
      autoreconf = project.binaryProperty("autoreconf", autoreconf)
    }

    project.tasks.register("xtrasConfig") {
      group = XTRAS_TASK_GROUP
      description = "Prints out the xtras configuration details"

      doFirst {
        println(
          """

                Binaries:
                  $binaryPropertyPrefix.git:            ${binaries.git}
                  $binaryPropertyPrefix.wget:           ${binaries.wget}
                  $binaryPropertyPrefix.tar:            ${binaries.tar}
                  $binaryPropertyPrefix.go:             ${binaries.go}
                  $binaryPropertyPrefix.autoreconf:     ${binaries.autoreconf}
                  $binaryPropertyPrefix.make:           ${binaries.make}
                  $binaryPropertyPrefix.cmake:          ${binaries.cmake}

                Paths:
                  ${XtrasPath.XTRAS.propertyName}:        ${project.xtrasDir}
                  ${XtrasPath.LIBS.propertyName}:       ${project.xtrasLibsDir}
                  ${XtrasPath.DOWNLOADS.propertyName}:  ${project.xtrasDownloadsDir}
                  ${XtrasPath.SOURCE.propertyName}:  ${project.xtrasSourceDir}
                  ${XtrasPath.PACKAGES.propertyName}:   ${project.xtrasPackagesDir}
                  ${XtrasPath.DOCS.propertyName}:       ${project.xtrasDocsDir}
                  ${XtrasPath.INTEROPS.propertyName}:   ${project.xtrasCInteropsDir}
                  ${XtrasPath.LOGS.propertyName}:       ${project.xtrasLogsDir}
                  ${XtrasPath.MAVEN.propertyName}:      ${project.xtrasMavenDir}
                  ${XtrasPath.NDK.propertyName}:        ${project.xtrasNdkDir}
                  
                  
                BuildEnvironment:
                  androidNdkApiVersion:     $androidNdkApiVersion
                  androidNdkDir:            $androidNdkDir (${XtrasPath.NDK.propertyName})
                  
                """.trimIndent()
        )
      }
    }
  }
}

const val XTRAS_EXTN_BUILD_ENVIRONMENT = "xtrasBuildEnvironment"

fun Project.xtrasBuildEnvironment(configure: BuildEnvironment.() -> Unit = {}): BuildEnvironment =

  extensions.findByType<BuildEnvironment>()?.also {
    return (it.clone() as BuildEnvironment).also(configure)
  } ?: extensions.create<BuildEnvironment>(XTRAS_EXTN_BUILD_ENVIRONMENT).apply {
    initialize(this@xtrasBuildEnvironment)

    configure()
  }

private val File.filePath: String
  get() = absolutePath.replace('\\', '/')