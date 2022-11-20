@file:Suppress("unused")

package org.danbrough.kotlinxtras

import org.danbrough.kotlinxtras.binaries.*
import org.gradle.api.Plugin
import org.gradle.api.Project


class IconvPlugin : Plugin<Project> {
  override fun apply(project: Project) {

    project.registerBinariesExtension("iconv").apply {
      version = "1.17c"

      downloadSources("https://ftp.gnu.org/pub/gnu/libiconv/libiconv-1.17.tar.gz") {
        stripTopDir = true
        tarExtractOptions = "xfz"
      }

      configureSources {target->
        val sourcesDir = sourcesDir(target)
        commandLine("./configure", "-C", "--enable-static", "--host=${target.hostTriplet}", "--prefix=${prefixDir(target)}")
        outputs.file(sourcesDir.resolve("Makefile"))
      }

      buildSources {target->
        commandLine("make","install")
        outputs.dir(prefixDir(target))
      }

    }
  }
}




