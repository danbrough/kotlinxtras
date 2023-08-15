@file:Suppress("unused")

package org.danbrough.kotlinxtras.core


import org.danbrough.kotlinxtras.binaries.*
import org.danbrough.kotlinxtras.hostTriplet
import org.danbrough.kotlinxtras.platformName
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.configurationcache.extensions.capitalized


const val LIBSSH2_EXTN_NAME = "libssh2"

//const val LIBSSH2_VERSION = "3.1.2-danbrough"
//const val LIBSSH2_GIT_COMMIT = "5accd111b0d4689a978f2bbccd976583f493efab"
const val LIBSSH2_VERSION = "1.11.0"
const val LIBSSH2_GIT_COMMIT = "1c3f1b7da588f2652260285529ec3c1f1125eb4e"
const val LIBSSH2_GIT_URL = "https://github.com/libssh2/libssh2.git"
//
//const val LIBSSH2_VERSION = "3.0.8"
//const val LIBSSH2_GIT_COMMIT = "e4e4c3b72620cf8ef35c275271415bfc675ffaa3"

fun Project.enableLibSSH2(
  openSSL: LibraryExtension,
  extnName: String = LIBSSH2_EXTN_NAME,
  versionName: String = LIBSSH2_VERSION,
  commit: String = LIBSSH2_GIT_COMMIT,
  gitURL: String = LIBSSH2_GIT_URL,
  config: LibraryExtension.() -> Unit = {}
): LibraryExtension =
  extensions.findByName(extnName) as? LibraryExtension ?: registerLibraryExtension(extnName) {
    publishingGroup = CORE_PUBLISHING_PACKAGE

    version = versionName

    git(gitURL, commit)


    val autoConfTaskName: org.jetbrains.kotlin.konan.target.KonanTarget.() -> String =
      { "xtrasAutoconf${libName.capitalized()}${platformName.capitalized()}" }


    configureTarget { target ->
      project.tasks.create(target.autoConfTaskName(), Exec::class.java) {
        dependsOn(openSSL.provideArchiveTaskName(target))
        dependsOn(extractSourcesTaskName(target))
        workingDir(sourcesDir(target))
        outputs.file(workingDir.resolve("configure"))
        commandLine(binaries.autoreconfBinary, "-fi")
      }
    }

    configure(override = true) { target ->

      dependsOn(openSSL.extractArchiveTaskName(target))

      dependsOn(target.autoConfTaskName())



      outputs.file(workingDir.resolve("Makefile"))

      commandLine(
        "./configure",
        "--with-crypto=openssl", "--with-libssl-prefix=${openSSL.libsDir(target)}",
        "--with-libz",
        //--with-libz-prefix[=DIR]  search for libz in DIR/include and DIR/lib
        "--without-libz-prefix",
        "--host=${target.hostTriplet}",
        "--prefix=${buildDir(target)}"
      )
    }

    build {
      commandLine(binaries.makeBinary, "install")
    }

    cinterops {
      headers = """
          headers =libssh2.h 
          linkerOpts =  -lz -lssl -lcrypto -lssh2
          #staticLibraries.linux = libcurl.a
          #staticLibraries.android = libcurl.a
          
          """.trimIndent()
    }

    config()
  }

