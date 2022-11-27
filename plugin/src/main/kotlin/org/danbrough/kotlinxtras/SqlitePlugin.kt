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
      version = "3.40.0"

      download("https://www.sqlite.org/2022/sqlite-autoconf-3400000.tar.gz") {
        stripTopDir = true
        tarExtractOptions = "xfz"
      }

     // git("https://github.com/sqlite/sqlite.git","2f2c5e2061cfebfad6b9aca4950d960caec073d8")

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

      cinterops {
        headers = """
          headers = sqlite3.h sqlite3ext.h
          linkerOpts = -ldl -lsqlite3
          linkerOpts.mingw = -ldl -lsqlite3 -lpthread
          headers = sqlite3.h
          headerFilter = sqlite3*.h
          
          """.trimIndent()
      }
    }
  }
}



