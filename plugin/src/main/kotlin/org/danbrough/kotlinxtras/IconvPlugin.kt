@file:Suppress("unused")

package org.danbrough.kotlinxtras



import org.danbrough.kotlinxtras.binaries.*
import org.gradle.api.Plugin
import org.gradle.api.Project

const val XTRAS_ICONV_EXTN_NAME = "xtrasIconv"

open class IconvBinaryExtension(project: Project) : LibraryExtension(project,"iconv")

class IconvPlugin : Plugin<Project> {
  override fun apply(project: Project) {

    project.registerLibraryExtension(XTRAS_ICONV_EXTN_NAME,IconvBinaryExtension::class.java) {
      version = "1.17c"

      download("https://ftp.gnu.org/pub/gnu/libiconv/libiconv-1.17.tar.gz") {
        stripTopDir = true
        tarExtractOptions = "xfz"
      }

      configure {target->
        val sourcesDir = sourcesDir(target)
        commandLine("./configure", "-C", "--enable-static", "--host=${target.hostTriplet}", "--prefix=${prefixDir(target)}")
        outputs.file(sourcesDir.resolve("Makefile"))
      }

      build {target->
        commandLine("make","install")
        outputs.dir(prefixDir(target))
      }
    }
  }
}




