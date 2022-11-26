@file:Suppress("unused")

package org.danbrough.kotlinxtras


import org.danbrough.kotlinxtras.binaries.*
import org.gradle.api.Plugin
import org.gradle.api.Project

const val XTRAS_SQLITE_EXTN_NAME = "xtrasSqlite"

open class SqliteBinaryExtension(project: Project) : LibraryExtension(project,"sqlite")

class SqlitePlugin : Plugin<Project> {

  override fun apply(project: Project) {
    project.registerLibraryExtension(XTRAS_SQLITE_EXTN_NAME,SqliteBinaryExtension::class.java) {
      version = "3.39.4"

      download("https://www.sqlite.org/2022/sqlite-autoconf-3390400.tar.gz") {
        stripTopDir = true
        tarExtractOptions = "xfz"
      }

      configure { target->
        commandLine(
          "./configure",
          "--host=${target.hostTriplet}",
          "--disable-readline",
          "--prefix=${prefixDir(target)}",
        )
        outputs.file(workingDir.resolve("Makefile"))
      }

      build {
        commandLine(binaryConfiguration.makeBinary,"install")
      }
    }
  }
}



