@file:Suppress("unused")

package org.danbrough.kotlinxtras


import org.danbrough.kotlinxtras.binaries.*
import org.gradle.api.Project

const val XTRAS_ICONV_EXTN_NAME = "iconv"


fun Project.enableIconv(name: String = XTRAS_ICONV_EXTN_NAME,config:LibraryExtension.()->Unit = {}) =
  project.registerLibraryExtension(name) {
    publishingGroup = CORE_PUBLISHING_PACKAGE
    version = "1.17"

    download("https://ftp.gnu.org/pub/gnu/libiconv/libiconv-1.17.tar.gz") {
      stripTopDir = true
    }

    configure { target ->
      commandLine(
        "./configure",
        "-C",
        "--enable-static",
        "--host=${target.hostTriplet}",
        "--prefix=${buildDir(target)}"
      )
      outputs.file(workingDir.resolve("Makefile"))
    }

    build {
      commandLine(binaries.makeBinary, "install")
    }

    cinterops {
      headers = """
          |headers = iconv.h libcharset.h
          |linkerOpts = -liconv
          |package = libiconv
          |""".trimMargin()
    }

    config()
  }





