# KotlinXtras

Common Kotlin packages with support for linux_arm32_hfp, linux_arm64 and Android native.


## Description

This repo is serving two purposes at the moment.
One is for tweaks of popular kotlin multiplatform packages for android native and linuxArm64, linuxArm32Hfp support.
That you'll find in the [repos](./repos/) folder.

The other is a complimentary plugin for cross-compiling native libraries for kotlin multiplatform.

Precompiled versions will be published at `mavenCentral()` under [my repository](https://repo.maven.apache.org/maven2/org/danbrough/).  
They might take a while to get there so pre-releases can be found in [the sonatype staging section](https://s01.oss.sonatype.org/content/groups/staging/org/danbrough/)  
So you don't need to download this project and compile it, unless you want to.  

To use them in your projects just add: 

```gradle 
repositories {
 // for the pre releases
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  // for the final releases
  mavenCentral()
}

//and change "org.jetbrains.kotlinx" to "org.danbrough.kotlinx"
dependencies {
  implementation("org.danbrough.kotlinx:kotlinx-coroutines-core:1.6.4")
  implementation("org.danbrough.kotlinx:kotlinx-serialization-core:1.3.3")
  // ..etc 
}
```

The tweaked repos will be git submodules under the [repos](./repos) folder.
So you can clone those, compare with upstream, roll your own.
Due to the need for absolute hard-coded paths in cinterop def files this repo will need to be checked out at /usr/local/kotlinxtras
for building custom packages.  
You can also use the "consumer" plugin from this project to automatically download precompiled binaries to link against.  
See the sqlite,curl_standalone,sqldelight in the [demos](./demos) folder for example.

## The plugin.

The plugin is used for cross-compiling kotlin multiplatform projects with native library support.
There are demos in the [demos](./demos/) folder but it looks something like this:  

```kotlin

const val XTRAS_OPENSSL_EXTN_NAME = "xtrasOpenssl"

open class OpenSSLBinaryExtension(project: Project) : LibraryExtension(project, "openssl")

class OpenSSLPlugin : Plugin<Project> {
  override fun apply(project: Project) {

    project.registerLibraryExtension(XTRAS_OPENSSL_EXTN_NAME, OpenSSLBinaryExtension::class.java) {

      version = "1_1_1s"

      git("https://github.com/danbrough/openssl.git", "02e6fd7998830218909cbc484ca054c5916fdc59")

      configure { target ->
        outputs.file(workingDir.resolve("Makefile"))
        val args = mutableListOf(
          "./Configure",
          target.opensslPlatform,
          "no-tests",
          "threads",
          "--prefix=${prefixDir(target)}"
        )
        if (target.family == Family.ANDROID) args += "-D__ANDROID_API__=21"
        else if (target.family == Family.MINGW) args += "--cross-compile-prefix=${target.hostTriplet}-"

        commandLine(args)
      }


      build { target ->
        commandLine(binaryConfiguration.makeBinary, "install_sw")
      }

      cinterops {
        headers = """
          #staticLibraries =  libcrypto.a libssl.a
          headers = openssl/ssl.h openssl/err.h openssl/bio.h openssl/evp.h
          linkerOpts.linux = -ldl -lc -lm -lssl -lcrypto
          linkerOpts.android = -ldl -lc -lm -lssl -lcrypto
          linkerOpts.macos = -ldl -lc -lm -lssl -lcrypto
          linkerOpts.mingw = -lm -lssl -lcrypto
          compilerOpts.android = -D__ANDROID_API__=21
          compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
          #compilerOpts = -static
          
          """.trimIndent()
      }
    }
  }
}
```

That creates a plugin that allows you to automatically download openSSL sources, cross compile them 
and build cinterop bindings for kotlin.
You can also download precompiled binaries from maven.

In the case of curl,openssl,iconv,sqlite (at present) you don't need to write these yourself as they
are provided, (with precompiled binaries for linuxArm32Hfp,linuuxArm64, linuxX64, macosX64,macosArm64,androidNativeX86 .. and the rest ..)

But as you can see, writing your own for projects using the "./configure, make install" logic isn't too hard.


## News


The [sqldelight](./demos/sqldelight) demo is working with sqldelight 2.0.0-alpha04 with all linux and android native targets.









