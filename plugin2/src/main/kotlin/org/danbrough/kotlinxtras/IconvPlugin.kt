@file:Suppress("unused")

package org.danbrough.kotlinxtras

import org.gradle.api.Plugin
import org.gradle.api.Project


class IconvPlugin : Plugin<Project> {
  override fun apply(project: Project) {

    project.registerBinariesExtension("iconv").apply {
      //sourceURL = "https://github.com/danbrough/openssl"
      version = "1.17c"
      sourceURL = "https://ftp.gnu.org/pub/gnu/libiconv/libiconv-1.17.tar.gz"

      archiveConfig {
        stripTopDir = true
        tarExtractOptions = "xfz"
      }
      //  gitSource("02e6fd7998830218909cbc484ca054c5916fdc59")
    }


  }
}




