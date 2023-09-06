@file:Suppress("MemberVisibilityCanBePrivate")

package org.danbrough.xtras.env

import org.danbrough.xtras.PROPERTY_CINTEROPS_DIR
import org.danbrough.xtras.PROPERTY_DOCS_DIR
import org.danbrough.xtras.PROPERTY_DOWNLOADS_DIR
import org.danbrough.xtras.PROPERTY_LIBS_DIR
import org.danbrough.xtras.PROPERTY_NDK_DIR
import org.danbrough.xtras.PROPERTY_PACKAGES_DIR
import org.danbrough.xtras.PROPERTY_XTRAS_DIR
import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.XtrasDSLMarker
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.log
import org.danbrough.xtras.projectProperty
import org.danbrough.xtras.xtrasCInteropsDir
import org.danbrough.xtras.xtrasDir
import org.danbrough.xtras.xtrasDocsDir
import org.danbrough.xtras.xtrasDownloadsDir
import org.danbrough.xtras.xtrasLibsDir
import org.danbrough.xtras.xtrasNdkDir
import org.danbrough.xtras.xtrasPackagesDir
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.util.Date


data class Binaries(

  @XtrasDSLMarker
  var git: String = "git",

  @XtrasDSLMarker
  var wget: String = "wget",

  @XtrasDSLMarker
  var tar: String = "tar",

  @XtrasDSLMarker
  var autoreconf: String = "autoreconf",

  @XtrasDSLMarker
  var make: String = "make",

  @XtrasDSLMarker
  var cmake: String = "cmake",

  @XtrasDSLMarker
  var go: String = "go",

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

    put("PATH", basePath.joinToString(File.pathSeparator))

    put("MAKE", "make -j${Runtime.getRuntime().availableProcessors() + 1}")

    put("CFLAGS", "-O3 -pthread -Wno-macro-redefined -Wno-deprecated-declarations")

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
        if (HostManager.hostIsLinux)
          clangArgs =
            "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot"
      }

      KonanTarget.LINUX_ARM32_HFP -> {
        if (HostManager.hostIsLinux)
          clangArgs =
            "--target=${target.hostTriplet} --gcc-toolchain=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2/arm-unknown-linux-gnueabihf/sysroot"
      }

//dan /usr/local/kotlinxtras $ ls ~/.konan/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2/arm-unknown-linux-gnueabihf/
//bin/        debug-root/ include/    lib/        sysroot/
//dan@dan /usr/local/kotlinxtras $ ls ~/.konan/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2/arm-unknown-linux-gnueabihf/sysroot/

      KonanTarget.MACOS_X64, KonanTarget.MACOS_ARM64, KonanTarget.WATCHOS_X64, KonanTarget.WATCHOS_ARM64, KonanTarget.IOS_X64, KonanTarget.IOS_ARM64 -> {
        put("CC", "clang")
        //put("CXX", "g++")
        //put("LD", "lld")
      }

      KonanTarget.MINGW_X64 -> {

      }

      KonanTarget.ANDROID_X64, KonanTarget.ANDROID_X86, KonanTarget.ANDROID_ARM64, KonanTarget.ANDROID_ARM32 -> {
        //library.project.log("ADDING NDK TO PATH")
        val archFolder = if (HostManager.hostIsLinux) "linux-x86_64" else "darwin-x86_64"
        put(
          "PATH",
          "${androidNdkDir.resolve("toolchains/llvm/prebuilt/$archFolder/bin").absolutePath}:${
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
    if (target != null)
      environmentForTarget(target)
  }

  @XtrasDSLMarker
  fun binaries(config: Binaries.() -> Unit) {
    binaries.config()
  }

  internal fun initialize(project: Project) {

    konanDir = System.getenv("KONAN_DATA_DIR")?.let { File(it) } ?: File(
      System.getProperty("user.home"),
      ".konan"
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
                  $PROPERTY_XTRAS_DIR:            ${project.xtrasDir}
                  $PROPERTY_LIBS_DIR:       ${project.xtrasLibsDir}
                  $PROPERTY_DOWNLOADS_DIR:  ${project.xtrasDownloadsDir}
                  $PROPERTY_PACKAGES_DIR:   ${project.xtrasPackagesDir}
                  $PROPERTY_DOCS_DIR:       ${project.xtrasDocsDir}
                  $PROPERTY_CINTEROPS_DIR:  ${project.xtrasCInteropsDir}
                  
                  
                BuildEnvironment:
                  androidNdkApiVersion:     $androidNdkApiVersion
                  androidNdkDir:            $androidNdkDir ($PROPERTY_NDK_DIR)
                  
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


/*
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
        env["CXX"] = "clang++ $clangArgs"
        /*        env["RANLIB"] =
                    "$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/bin/ranlib"*/
      }


      KonanTarget.MACOS_X64, KonanTarget.MACOS_ARM64, KonanTarget.WATCHOS_X64, KonanTarget.WATCHOS_ARM64, KonanTarget.IOS_X64, KonanTarget.IOS_ARM64 -> {
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
        error("Unsupported target: $target")
      }
    }

    if (HostManager.hostIsMac) basePath.add(
      0, konanDir.resolve("dependencies/apple-llvm-20200714-macos-x64-essentials/bin").absolutePath
    ) else if (HostManager.hostIsLinux) {
      basePath.add(
        0, konanDir.resolve("dependencies/llvm-11.1.0-linux-x64-essentials/bin").absolutePath
      )
    }

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

 */

