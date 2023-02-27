@file:Suppress("unused")

package org.danbrough.kotlinxtras


import org.danbrough.kotlinxtras.binaries.*
import org.gradle.api.Project

const val XTRAS_SQLITE_EXTN_NAME = "sqlite"
const val XTRAS_SQLITE_VERSION_NAME = "3.40.1a"
const val XTRAS_SQLITE_SOURCE_URL = "https://www.sqlite.org/2022/sqlite-autoconf-3400100.tar.gz"

fun Project.enableSqlite(
  extnName: String = XTRAS_SQLITE_EXTN_NAME, versionName: String = XTRAS_SQLITE_VERSION_NAME,
  sourceURL: String = XTRAS_SQLITE_SOURCE_URL,
  config: LibraryExtension .() -> Unit = {}
): LibraryExtension =
  extensions.findByName(extnName) as? LibraryExtension ?: registerLibraryExtension(extnName) {
    publishingGroup = CORE_PUBLISHING_PACKAGE
    version = versionName

    download(sourceURL) {
      stripTopDir = true
      tarExtractOptions = "xfz"
    }

    configure { target ->
      dependsOn(target.registerKonanDepsTask(project))
      commandLine(
        "./configure",
        "--host=${target.hostTriplet}",
        "--disable-readline",
        "--prefix=${buildDir(target)}",
      )
      outputs.file(workingDir.resolve("Makefile"))
    }

    build {
      commandLine(binaries.makeBinary, "install")
    }

    cinterops {
      headers = """
          headers = sqlite3.h sqlite3ext.h
          linkerOpts = -ldl -lsqlite3
          linkerOpts.mingw = -ldl -lsqlite3 -lpthread
          headers = sqlite3.h
          headerFilter = sqlite3*.h
          compilerOpts = -fPIC 
          
          """.trimIndent()
    }

    config()
  }




