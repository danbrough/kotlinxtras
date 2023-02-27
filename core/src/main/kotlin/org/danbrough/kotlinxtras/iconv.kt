@file:Suppress("unused")

package org.danbrough.kotlinxtras


import org.danbrough.kotlinxtras.binaries.*
import org.gradle.api.Project

const val XTRAS_ICONV_EXTN_NAME = "iconv"
const val XTRAS_ICONV_VERSION_NAME = "1.17a"
const val XTRAS_ICONV_SOURCE_URL = "https://ftp.gnu.org/pub/gnu/libiconv/libiconv-1.17.tar.gz"

fun Project.enableIconv(
  extnName: String = XTRAS_ICONV_EXTN_NAME,
  versionName: String = XTRAS_ICONV_VERSION_NAME,
  sourceURL: String = XTRAS_ICONV_SOURCE_URL,
  config: LibraryExtension.() -> Unit = {}
) =
  extensions.findByName(extnName) as? LibraryExtension
    ?: project.registerLibraryExtension(extnName) {
      publishingGroup = CORE_PUBLISHING_PACKAGE
      version = versionName

      download(sourceURL) {
        stripTopDir = true
      }

      configure { target ->
        dependsOn(target.registerKonanDepsTask(project))
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





