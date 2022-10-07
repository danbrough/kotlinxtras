import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import BuildEnvironment.platformName

object OpenSSL {
  
  const val GIT_SRC = "https://github.com/danbrough/openssl"
  
  fun KonanTarget.opensslSrcDir(project: Project): File =
    project.rootProject.file("openssl/build/openssl/$platformName")
  
  fun KonanTarget.opensslPrefix(project: Project): File =
    project.rootProject.file("libs/openssl//$platformName")
  
  val KonanTarget.opensslPlatform: String
    get() = when (this) {
      KonanTarget.LINUX_X64 -> "linux-x86_64"
      KonanTarget.LINUX_ARM64 -> "linux-aarch64"
      KonanTarget.LINUX_ARM32_HFP -> "linux-armv4"
      KonanTarget.LINUX_MIPS32 -> TODO()
      KonanTarget.LINUX_MIPSEL32 -> TODO()
      KonanTarget.ANDROID_ARM32 -> "android-arm"
      KonanTarget.ANDROID_ARM64 -> "android-arm64"
      KonanTarget.ANDROID_X86 -> "android-x86"
      KonanTarget.ANDROID_X64 -> "android-x86_64"
      KonanTarget.MINGW_X64 -> "mingw64"
      KonanTarget.MINGW_X86 -> "mingw"

      KonanTarget.MACOS_X64 -> "darwin64-x86_64-cc"
      KonanTarget.MACOS_ARM64 -> "darwin64-arm64-cc"
      KonanTarget.IOS_ARM32 -> "ios-cross"
      KonanTarget.IOS_ARM64 -> "ios64-cross" //ios-cross ios-xcrun
      KonanTarget.IOS_SIMULATOR_ARM64 -> "iossimulator-xcrun"
      KonanTarget.IOS_X64 -> "ios64-cross"

      KonanTarget.TVOS_ARM64 -> TODO()
      KonanTarget.TVOS_SIMULATOR_ARM64 -> TODO()
      KonanTarget.TVOS_X64 -> TODO()
      KonanTarget.WASM32 -> TODO()
      KonanTarget.WATCHOS_ARM32 -> TODO()
      KonanTarget.WATCHOS_ARM64 -> TODO()
      KonanTarget.WATCHOS_SIMULATOR_ARM64 -> TODO()
      KonanTarget.WATCHOS_X64 -> TODO()
      KonanTarget.WATCHOS_X86 -> TODO()
      is KonanTarget.ZEPHYR -> TODO()
    }
}

/*
BS2000-OSD BSD-aarch64 BSD-generic32 BSD-generic64 BSD-ia64 BSD-riscv64
BSD-sparc64 BSD-sparcv8 BSD-x86 BSD-x86-elf BSD-x86_64 Cygwin Cygwin-i386
Cygwin-i486 Cygwin-i586 Cygwin-i686 Cygwin-x86 Cygwin-x86_64 DJGPP MPE/iX-gcc
UEFI UWIN VC-CE VC-WIN32 VC-WIN32-ARM VC-WIN32-ONECORE VC-WIN64-ARM VC-WIN64A
VC-WIN64A-ONECORE VC-WIN64A-masm VC-WIN64I aix-cc aix-gcc aix64-cc aix64-gcc
android-arm android-arm64 android-armeabi android-mips android-mips64
android-x86 android-x86_64 android64 android64-aarch64 android64-mips64
android64-x86_64 bsdi-elf-gcc cc darwin-i386-cc darwin-ppc-cc
darwin64-arm64-cc darwin64-debug-test-64-clang darwin64-ppc-cc
darwin64-x86_64-cc gcc haiku-x86 haiku-x86_64 hpux-ia64-cc hpux-ia64-gcc
hpux-parisc-cc hpux-parisc-gcc hpux-parisc1_1-cc hpux-parisc1_1-gcc
hpux64-ia64-cc hpux64-ia64-gcc hpux64-parisc2-cc hpux64-parisc2-gcc hurd-x86
ios-cross ios-xcrun ios64-cross ios64-xcrun iossimulator-xcrun iphoneos-cross
irix-mips3-cc irix-mips3-gcc irix64-mips4-cc irix64-mips4-gcc linux-aarch64
linux-alpha-gcc linux-aout linux-arm64ilp32 linux-armv4 linux-c64xplus
linux-elf linux-generic32 linux-generic64 linux-ia64 linux-mips32 linux-mips64
linux-ppc linux-ppc64 linux-ppc64le linux-sparcv8 linux-sparcv9 linux-x32
linux-x86 linux-x86-clang linux-x86_64 linux-x86_64-clang linux32-s390x
linux64-mips64 linux64-riscv64 linux64-s390x linux64-sparcv9 mingw mingw64
nextstep nextstep3.3 purify sco5-cc sco5-gcc solaris-sparcv7-cc
solaris-sparcv7-gcc solaris-sparcv8-cc solaris-sparcv8-gcc solaris-sparcv9-cc
solaris-sparcv9-gcc solaris-x86-gcc solaris64-sparcv9-cc solaris64-sparcv9-gcc
solaris64-x86_64-cc solaris64-x86_64-gcc tru64-alpha-cc tru64-alpha-gcc
uClinux-dist uClinux-dist64 unixware-2.0 unixware-2.1 unixware-7
unixware-7-gcc vms-alpha vms-alpha-p32 vms-alpha-p64 vms-ia64 vms-ia64-p32
vms-ia64-p64 vos-gcc vxworks-mips vxworks-ppc405 vxworks-ppc60x vxworks-ppc750
vxworks-ppc750-debug vxworks-ppc860 vxworks-ppcgen vxworks-simlinux debug
debug-erbridge debug-linux-ia32-aes debug-linux-pentium debug-linux-ppro
debug-test-64-clang


 */