import org.danbrough.kotlinxtras.binaries.download
import org.danbrough.kotlinxtras.binaries.git
import org.danbrough.kotlinxtras.binaries.registerLibraryExtension
import org.danbrough.kotlinxtras.core.enableCurl
import org.danbrough.kotlinxtras.core.enableLibSSH2
import org.danbrough.kotlinxtras.core.enableWolfSSL
import org.danbrough.xtras.hostTriplet
import org.danbrough.xtras.log
import org.danbrough.xtras.platformName
import org.gradle.configurationcache.extensions.capitalized

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  //alias(libs.plugins.kotlinXtras)
  id("org.danbrough.kotlinxtras.core") version "0.0.3-beta19"
  id("org.danbrough.kotlinxtras.binaries") version "0.0.3-beta19"

}


repositories {
  maven("/usr/local/kotlinxtras/build/xtras/maven")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}
val javaLangVersion = 8

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(javaLangVersion))
}

kotlin {
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(javaLangVersion))
  }
}

val ssl = enableWolfSSL {
  version = "5.1.1"
  git("https://github.com/wolfSSL/wolfssl.git", "c3513bf2573c30f6d2df815de216120e92142020")

  publishBinaries = true
  deferToPrebuiltPackages = false

  val autogenTaskName: org.jetbrains.kotlin.konan.target.KonanTarget.() -> String =
    { "xtrasAutogen${libName.capitalized()}${platformName.capitalized()}" }


  configure { target ->
    //binaries.androidNdkDir = File("/mnt/files/sdk/android/ndk/25.0.8775105/")

    outputs.file(workingDir.resolve("Makefile"))


    dependsOn(target.autogenTaskName())
    val configureOptions = mutableListOf(
      "./configure",
      "--host=${target.hostTriplet}",
      "--prefix=${buildDir(target)}",
      "--enable-all",
////      "--disable-fasthugemath",
////      "--disable-bump",
////      "--enable-opensslextra",
////      "--enable-fortress",
////      "--disable-debug",
////      "--disable-ntru",
////      "--disable-examples",
////      "--enable-distro",
////      "--enable-reproducible-build",
//      "--enable-curve25519",
//      "--enable-ed25519",
//      "--enable-curve448",
//      "--enable-ed448",
//      "--enable-sha512",
//      "--with-max-rsa-bits=8192",
//
//
////  --enable-certreq        Enable cert request generation (default: disabled)
//      "--enable-certext",//        Enable cert request extensions (default: disabled)
////  --enable-certgencache   Enable decoded cert caching (default: disabled)
//      //--enable-altcertchains  Enable using alternative certificate chains, only
//      //   require leaf certificate to validate to trust root
//      //--enable-testcert       Enable Test Cert (default: disabled)
//      "--enable-certservice",
//      "--enable-altcertchains",
////      "--enable-writedup",
//
//      "--enable-opensslextra",
//      "--enable-openssh",
//      "--enable-libssh2",
//      "--enable-keygen", "--enable-certgen",
//      "--enable-ssh", "--enable-wolfssh",
//      "--disable-examples", "--enable-postauth",

    )

    if (target != org.jetbrains.kotlin.konan.target.KonanTarget.MINGW_X64)
      configureOptions.add("--enable-jni")



    project.log("configuring with $configureOptions CC is ${environment["CC"]} CFLAGS: ${environment["CFLAGS"]}")

    when (target) {
      org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_ARM32, org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_ARM64, org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_X64, org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_X86 -> {
        environment(
          "PATH",
          "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
        )
      }

      else -> {}
    }

    commandLine(configureOptions)
  }
  cinterops {
    headers = """
          staticLibraries =  libwolfssl.la
          headers =  wolfssl/ssl.h wolfssl/openssl/ssl.h wolfssl/openssl/err.h wolfssl/openssl/bio.h wolfssl/openssl/evp.h
          linkerOpts.linux = -ldl -lc -lm -lwolfssl 
          #linkerOpts.android = -ldl -lc -lm -lwolfssl
          #linkerOpts.macos = -ldl -lc -lm -lwolfssl 
          #linkerOpts.mingw = -lm -lwolfssl 
          compilerOpts.android = -D__ANDROID_API__=21
          #compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
          #compilerOpts = -static
          
          """.trimIndent()
  }
}



registerLibraryExtension("wolfSSH") {
  deferToPrebuiltPackages = false
  publishBinaries = true

  publishingGroup = org.danbrough.kotlinxtras.core.CORE_PUBLISHING_PACKAGE

  version = "1.4.14"
  git("https://github.com/wolfSSL/wolfssh.git", "31c98b8c68acb10b755c3bfc008d6294de017586")
  val autogenTaskName: org.jetbrains.kotlin.konan.target.KonanTarget.() -> String =
    { "xtrasAutogen${libName.capitalized()}${platformName.capitalized()}" }

  configureTarget { target ->
    project.tasks.create(target.autogenTaskName(), Exec::class.java) {
      dependsOn(extractSourcesTaskName(target))
      workingDir(sourcesDir(target))
      outputs.file(workingDir.resolve("configure"))
      commandLine("./autogen.sh")

      environment(
        "PATH",
        "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
      )
    }
  }

  configure { target ->
    //binaries.androidNdkDir = File("/mnt/files/sdk/android/ndk/25.0.8775105/")

    outputs.file(workingDir.resolve("Makefile"))
    dependsOn(target.autogenTaskName())

    val configureOptions = mutableListOf(
      "./configure",
      "--host=${target.hostTriplet}",
      "--prefix=${buildDir(target)}",
      "--enable-all",
      "--with-wolfssl=${ssl.libsDir(target)}",
      //"--disable-examples",
    )

    project.log("configuring with $configureOptions CC is ${environment["CC"]} CFLAGS: ${environment["CFLAGS"]}")

    when (target) {
      org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_ARM32, org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_ARM64, org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_X64, org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_X86 -> {
        environment(
          "PATH",
          "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
        )
      }

      else -> {}
    }

    commandLine(configureOptions)
  }

  build { target ->
    when (target) {
      org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_ARM32, org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_ARM64, org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_X64, org.jetbrains.kotlin.konan.target.KonanTarget.ANDROID_X86 -> {
        environment(
          "PATH",
          "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
        )
      }

      else -> {}
    }

    commandLine(binaries.makeBinary, "install")
  }
  cinterops {
    dependencies = listOf(ssl)
    interopsPackage = "wolfssh"

    headers = """
          #staticLibraries =  libwolfssh.la
          headers =  wolfssh/ssh.h 
          linkerOpts.linux = -lc -lm -lwolfssh -lwolfssl
          linkerOpts.android =  -lc -lm -lwolfssh
          linkerOpts.macos =  -lc -lm -lwolfssh
          linkerOpts.mingw = -lm -lwolfssh
          compilerOpts.linux_x64 = -I/usr/local/kotlinxtras/demos/libssh2/build/xtras/libs/wolfSSL/5.6.3/linuxX64/include
          compilerOpts.android = -D__ANDROID_API__=21
          #compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
          #compilerOpts = -static
          
          """.trimIndent()
  }

}





enableLibSSH2(ssl) {
  deferToPrebuiltPackages = false

  publishBinaries = true
  val autoConfTaskName: org.jetbrains.kotlin.konan.target.KonanTarget.() -> String =
    { "xtrasAutoconf${libName.capitalized()}${platformName.capitalized()}" }



  configure { target ->

    dependsOn(ssl.extractArchiveTaskName(target))

    dependsOn(target.autoConfTaskName())



    outputs.file(workingDir.resolve("Makefile"))

    commandLine(
      "./configure",
      "--with-crypto=wolfssl", "--with-libwolfssl-prefix=${ssl.libsDir(target)}",
      "--disable-examples-build",
      "--host=${target.hostTriplet}",
      "--prefix=${buildDir(target)}"
    )
  }

  build {
    commandLine(binaries.makeBinary, "install")
  }
  cinterops {
    interopsPackage = "libssh2"

    headersSource = """
      void print_test(char** msg){
        printf("print_test(): <%s>\n",*msg);
      }
      void ptr_test(char** msg){
              *msg = "Message from C!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";

              /**msg = malloc(256);
              memset(*msg,0,256);
              strncpy(*msg,"Hello World and stuff",44);*/
      }

      void ptr_free(char** msg){
        printf("doing a free\n");
        free(*msg);
      }
    """.trimIndent()

    headers = """
          headers = libssh2.h libssh2_publickey.h  libssh2_sftp.h
          linkerOpts =  -lz -lwolfssl -lssh2 -lm -lc -lz
          #staticLibraries.linux = libcurl.a
          #staticLibraries.android = libcurl.a
          
          """.trimIndent()
  }
}




registerLibraryExtension("curl2") {
  deferToPrebuiltPackages = false
  publishBinaries = true

  publishingGroup = org.danbrough.kotlinxtras.core.CORE_PUBLISHING_PACKAGE
  // binaries.androidNdkDir = File("/mnt/files/sdk/android/ndk/25.0.8775105/")


  version = "8.2.1"

  //git("https://github.com/curl/curl.git", "046209e561b7e9b5aab1aef7daebf29ee6e6e8c7")
  //git("https://github.com/curl/curl.git", "b16d1fa8ee567b52c09a0f89940b07d8491b881d")
  git("https://github.com/curl/curl.git", "50490c0679fcd0e50bb3a8fbf2d9244845652cf0")

  //binaries.androidNdkDir = File("/mnt/files/sdk/android/ndk/25.0.8775105/")


  val autoConfTaskName: org.jetbrains.kotlin.konan.target.KonanTarget.() -> String =
    { "xtrasAutoconf${libName.capitalized()}${platformName.capitalized()}" }

  configureTarget { target ->

    project.tasks.create(target.autoConfTaskName(), Exec::class.java) {
      dependsOn(extractSourcesTaskName(target))
      workingDir(sourcesDir(target))
      outputs.file(workingDir.resolve("configure"))
      commandLine(binaries.autoreconfBinary, "-fi")

      environment(
        "PATH",
        "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
      )
    }
  }



  configure { target ->
    dependsOn(project.tasks.getByName(ssl.extractArchiveTaskName(target)))

    dependsOn(target.autoConfTaskName())



    outputs.file(workingDir.resolve("Makefile"))

    val configureOptions = mutableListOf(
      "./configure",
      "--host=${target.hostTriplet}",
      "--with-wolfssl=${ssl.libsDir(target)}",
      "--enable-all",
      //"--with-ca-path=/etc/ssl/certs:/etc/security/cacerts:/etc/ca-certificates",
      "--prefix=${buildDir(target)}"
    )
    environment(
      "PATH",
      "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
    )

    commandLine(configureOptions)
  }

  build {
    environment(
      "PATH",
      "${binaries.androidNdkDir}/prebuilt/linux-x86_64/bin:${binaries.androidNdkDir}/toolchains/llvm/prebuilt/linux-x86_64/bin:${environment["PATH"]}"
    )
    commandLine(binaries.makeBinary, "install")
  }

  cinterops {
    dependencies = listOf(ssl)

    headers = """
          headers = curl/curl.h
          linkerOpts =  -lz -lwolfssl -lcurl
          #staticLibraries = libcurl.a libwolfssl.la
          #staticLibraries.android = libcurl.a
          
          """.trimIndent()
  }
}






kotlin {

  linuxX64()
  androidNativeArm64()
  linuxArm64()

  if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
  }

  //androidNativeX86()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(libs.org.danbrough.kotlinxtras.common)
      implementation(libs.org.danbrough.kotlinxtras.utils)

      //implementation(libs.io.ktor.ktorutils)
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget> {

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }

    binaries {
      executable("ssh2Demo") {
        entryPoint = "demo.ssh2.main"
        findProperty("args")?.also {
          runTask?.args(it.toString().split(','))
        }
      }

      executable("hexDemo") {
        entryPoint = "demo.hex.main"
        findProperty("args")?.also {
          runTask?.args(it.toString())
        }
      }

    }
  }
}



tasks.create("runCurl") {
  dependsOn("runCurlDemoDebugExecutable${if (HostManager.hostIsMac) "MacosX64" else "LinuxX64"}")
}
