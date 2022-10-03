@file:Suppress("MemberVisibilityCanBePrivate")

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTargetPreset
import org.jetbrains.kotlin.konan.target.Architecture
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

import java.io.File

object BuildEnvironment {

  val goBinary: String by ProjectProperties.createProperty("go.binary", "/usr/bin/go")

  val gitBinary: String by ProjectProperties.createProperty("git.binary", "/usr/bin/git")

  val buildCacheDir: File by ProjectProperties.createProperty("build.cache", "build/cache")


  val hostIsMac: Boolean by lazy {
    System.getProperty("os.name").startsWith("Mac")
  }

  val konanDir: File by ProjectProperties.createProperty(
    "konan.dir", "${System.getProperty("user.home")}/.konan"
  )

  val androidNdkDir: File by ProjectProperties.createProperty(
    "android.ndk.dir", konanDir.resolve(
      if (hostIsMac) "dependencies/target-toolchain-2-osx-android_ndk" else
        "dependencies/target-toolchain-2-linux-android_ndk"
    ).absolutePath
  )

  val androidNdkApiVersion: Int by ProjectProperties.createProperty("android.ndk.api.version", "21")

  private val buildPathEnvironment: String by ProjectProperties.createProperty("build.path")


  fun KotlinMultiplatformExtension.declareNativeTargets() {
    //comment out platforms you don't need
    androidNativeX86()
    androidNativeX64()
    androidNativeArm32()
    androidNativeArm64()

    linuxX64()
    linuxArm64()
    linuxArm32Hfp()

    macosX64()
    macosArm64()

    //iosArm32()
    iosArm64()
    iosSimulatorArm64()
    iosX64()

    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosX86()
    watchosSimulatorArm64()

    //supports openssl and curl but not sqlite.
    //weird compilation errors
    mingwX64()

  }


  val KonanTarget.platformName: String
    get() {
      if (family == Family.ANDROID) {
        return when (this) {
          KonanTarget.ANDROID_X64 -> "androidNativeX64"
          KonanTarget.ANDROID_X86 -> "androidNativeX86"
          KonanTarget.ANDROID_ARM64 -> "androidNativeArm64"
          KonanTarget.ANDROID_ARM32 -> "androidNativeArm32"
          else -> throw Error("Unhandled android target $this")
        }
      }
      return name.split("_").joinToString("") { it.capitalize() }.decapitalize()
    }


  val KonanTarget.hostTriplet: String
    get() = when (this) {
      KonanTarget.LINUX_ARM64 -> "aarch64-unknown-linux-gnu"
      KonanTarget.LINUX_X64 -> "x86_64-unknown-linux-gnu"
      KonanTarget.LINUX_ARM32_HFP -> "arm-linux-gnueabihf"
      KonanTarget.ANDROID_ARM32 -> "armv7a-linux-androideabi"
      KonanTarget.ANDROID_ARM64 -> "aarch64-linux-android"
      KonanTarget.ANDROID_X64 -> "x86_64-linux-android"
      KonanTarget.ANDROID_X86 -> "i686-linux-android"
      KonanTarget.MACOS_X64 -> "x86_64-apple-darwin"

      KonanTarget.MACOS_ARM64 -> "aarch64-apple-darwin"
      KonanTarget.MINGW_X64 -> "x86_64-w64-mingw32"
      KonanTarget.MINGW_X86 -> "x86-w64-mingw32"
      KonanTarget.IOS_ARM32 -> "arm32-apple-darwin"
      KonanTarget.IOS_ARM64 -> "aarch64-ios-darwin"
      //KonanTarget.IOS_SIMULATOR_ARM64 -> "aarch64-apple-darwin"
      KonanTarget.IOS_X64 -> "x86_64-ios-darwin"


      KonanTarget.TVOS_ARM64 -> "aarch64-tvos-darwin"
      //KonanTarget.TVOS_SIMULATOR_ARM64 -> "x86_64-tvos-darwin"
      KonanTarget.TVOS_X64 -> "x86_64-tvos-darwin"
      KonanTarget.WASM32 -> TODO()
      KonanTarget.WATCHOS_ARM32 -> "arm32-watchos-darwin"
      KonanTarget.WATCHOS_ARM64 -> "aarch64-watchos-darwin"
      KonanTarget.WATCHOS_SIMULATOR_ARM64 -> TODO()
      KonanTarget.WATCHOS_X64 -> "x86_64-watchos-darwin"
      KonanTarget.WATCHOS_X86 -> "x86-watchos-darwin"
      else -> TODO("Add hostTriple for $this")

    }

  val KonanTarget.androidLibDir: String?
    get() = when (this) {
      KonanTarget.ANDROID_ARM32 -> "armeabi-v7a"
      KonanTarget.ANDROID_ARM64 -> "arm64-v8a"
      KonanTarget.ANDROID_X64 -> "x86_64"
      KonanTarget.ANDROID_X86 -> "x86"
      else -> null
    }

  val KonanTarget.sharedLibExtn: String
    get() = when {
      family.isAppleFamily -> "dylib"
      family == Family.MINGW -> "dll"
      else -> "so"
    }

  val hostTarget: KonanTarget by lazy {

    val osName = System.getProperty("os.name")
    val osArch = System.getProperty("os.arch")
    val hostArchitecture: Architecture = when (osArch) {
      "amd64", "x86_64" -> Architecture.X64
      "arm64", "aarch64" -> Architecture.ARM64
      else -> throw Error("Unknown os.arch value: $osArch")
    }

    when {
      osName == "Linux" -> {
        when (hostArchitecture) {
          Architecture.ARM64 -> KonanTarget.LINUX_ARM64
          Architecture.X64 -> KonanTarget.LINUX_X64
          else -> null
        }
      }

      osName.startsWith("Mac") -> {
        when (hostArchitecture) {
          Architecture.X64 -> KonanTarget.MACOS_X64
          Architecture.ARM64 -> KonanTarget.MACOS_ARM64
          else -> null
        }
      }

      osName.startsWith("Windows") -> {
        when (hostArchitecture) {
          Architecture.X64 -> KonanTarget.MINGW_X64
          else -> null
        }
      }
      else -> null
    } ?: throw Error("Unknown build host: $osName:$osArch")
  }

  fun KotlinMultiplatformExtension.registerTarget(
    konanTarget: KonanTarget, conf: KotlinNativeTarget.() -> Unit = {}
  ): KotlinNativeTarget {
    @Suppress("UNCHECKED_CAST")
    val preset: KotlinTargetPreset<KotlinNativeTarget> =
      presets.getByName(konanTarget.platformName) as KotlinTargetPreset<KotlinNativeTarget>
    return targetFromPreset(preset, konanTarget.platformName, conf)
  }

  val androidToolchainDir by lazy {
    androidNdkDir.also {
      assert(it.exists()) {
        "Failed to locate ${it.absolutePath}"
      }
    }
  }


  val clangBinDir by lazy {
    File("$konanDir/dependencies").listFiles()?.first {
      it.isDirectory && it.name.contains("essentials")
    }?.let { it.resolve("bin") }
      ?: throw Error("Failed to locate clang folder in ${konanDir}/dependencies")
  }


  /*
  see:   go/src/go/build/syslist.go
  const goosList = "aix android darwin dragonfly freebsd hurd illumos
  ios js linux nacl netbsd openbsd plan9 solaris windows zos "
  const goarchList = "386 amd64 amd64p32 arm armbe arm64
  arm64be loong64 mips mipsle mips64 mips64le mips64p32 mips64p32le ppc
   ppc64 ppc64le riscv riscv64 s390 s390x sparc sparc64 wasm "
    */
  val KonanTarget.goOS: String?
    get() = when (family) {
      Family.OSX -> "darwin"
      Family.IOS, Family.TVOS, Family.WATCHOS -> "ios"
      Family.LINUX -> "linux"
      Family.MINGW -> "windows"
      Family.ANDROID -> "android"
      Family.WASM -> null
      Family.ZEPHYR -> null
    }

  val KonanTarget.goArch: String
    get() = when (architecture) {
      Architecture.ARM64 -> "arm64"
      Architecture.X64 -> "amd64"
      Architecture.X86 -> "386"
      Architecture.ARM32 -> "arm"
      Architecture.MIPS32 -> "mips" //TODO: confirm this
      Architecture.MIPSEL32 -> "mipsle" //TODO: confirm this
      Architecture.WASM32 -> "wasm"
    }

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

    this["KONAN_BUILD"] = 1

    this["ANDROID_NDK_HOME"] = androidNdkDir.absolutePath


    when (this@buildEnvironment) {

      KonanTarget.LINUX_ARM32_HFP -> {
        val clangArgs =
          "--target=$hostTriplet --gcc-toolchain=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=$konanDir/dependencies/arm-unknown-linux-gnueabihf-gcc-8.3.0-glibc-2.19-kernel-4.9-2/arm-unknown-linux-gnueabihf/sysroot "
        this["CC"] = "clang $clangArgs"
        this["CXX"] = "clang++ $clangArgs"
      }

      KonanTarget.LINUX_ARM64 -> {
        val clangArgs =
          "--target=$hostTriplet --gcc-toolchain=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2 --sysroot=$konanDir/dependencies/aarch64-unknown-linux-gnu-gcc-8.3.0-glibc-2.25-kernel-4.9-2/aarch64-unknown-linux-gnu/sysroot"
        this["CC"] = "clang $clangArgs"
        this["CXX"] = "clang++ $clangArgs"
      }

      KonanTarget.LINUX_X64 -> {
        val clangArgs =
          "--target=$hostTriplet --gcc-toolchain=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2 --sysroot=$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/sysroot"
        this["CC"] = "clang $clangArgs"
        this["CXX"] = "clang++ $clangArgs"
/*        this["RANLIB"] =
          "$konanDir/dependencies/x86_64-unknown-linux-gnu-gcc-8.3.0-glibc-2.19-kernel-4.9-2/x86_64-unknown-linux-gnu/bin/ranlib"*/
      }

      KonanTarget.MACOS_X64 -> {
        this["CC"] = "gcc"
        this["CXX"] = "g++"
      }


      KonanTarget.MINGW_X64 -> {
//        this["CC"] = "x86_64-w64-mingw32-gcc"
//        this["CXX"] = "x86_64-w64-mingw32-g++"

//        val clangArgs =
//          "--target=$hostTriplet --gcc-toolchain=$konanDir/dependencies/msys2-mingw-w64-x86_64-2/x86_64-w64-mingw32" +
//              " --sysroot=$konanDir/dependencies/msys2-mingw-w64-x86_64-2/x86_64-w64-mingw32/x86_64-w64-mingw32"
//        this["CC"] = "clang $clangArgs"
//        this["CXX"] = "clang++ $clangArgs"
        /*  export HOST=x86_64-w64-mingw32
  export GOOS=windows
  export CFLAGS="$CFLAGS -pthread"
  #export WINDRES=winres
  export WINDRES=/usr/bin/x86_64-w64-mingw32-windres
  export RC=$WINDRES
  export GOARCH=amd64
  export OPENSSL_PLATFORM=mingw64
  export LIBNAME="libkipfs.dll"
  #export PATH=/usr/x86_64-w64-mingw32/bin:$PATH
  export TARGET=$HOST
  #export PATH=$(dir_path bin $TOOLCHAIN):$PATH
  export CROSS_PREFIX=$TARGET-
  export CC=$TARGET-gcc
  export CXX=$TARGET-g++
        */
/*
        this["WINDRES"] = "x86_64-w64-mingw32-windres"
        this["RC"] = this["WINDRES"] as String*/
        /*this["CROSS_PREFIX"] = "${platform.host}-"
        val toolChain = "$konanDir/dependencies/msys2-mingw-w64-x86_64-1"
        this["PATH"] = "$toolChain/bin:${this["PATH"]}"*/

        //this["CC"] = "x86_64-w64-mingw32-gcc"
        //this["CXX"] = "x86_64-w64-mingw32-g++"


      }

      KonanTarget.ANDROID_X64, KonanTarget.ANDROID_X86, KonanTarget.ANDROID_ARM64, KonanTarget.ANDROID_ARM32 -> {
        path.add(0, androidToolchainDir.resolve("bin").absolutePath)
        this["CC"] = "$hostTriplet${androidNdkApiVersion}-clang"
        this["CXX"] = "$hostTriplet${androidNdkApiVersion}-clang++"
        this["AR"] = "llvm-ar"
        this["RANLIB"] = "ranlib"
      }
    }

    path.add(0, konanDir.resolve("dependencies/llvm-11.1.0-linux-x64-essentials/bin").absolutePath)
    this["PATH"] = path.joinToString(File.pathSeparator)
  }


  fun KonanTarget.konanDepsTask(project: Project): String = ":konandeps:$platformName"
}


  


