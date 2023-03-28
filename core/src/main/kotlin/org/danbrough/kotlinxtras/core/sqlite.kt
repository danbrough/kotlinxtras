@file:Suppress("unused")

package org.danbrough.kotlinxtras.core


import org.danbrough.kotlinxtras.binaries.*
import org.danbrough.kotlinxtras.hostTriplet
import org.gradle.api.Project

const val XTRAS_SQLITE_EXTN_NAME = "sqlite"
const val XTRAS_SQLITE_VERSION_NAME = "3.41.2"
const val XTRAS_SQLITE_SOURCE_URL = "https://www.sqlite.org/2023/sqlite-autoconf-3410200.tar.gz"
const val XTRAS_SQLITE_COMMIT = "e671c4fbc057f8b1505655126eaf90640149ced6"

fun Project.enableSqlite(
  extnName: String = XTRAS_SQLITE_EXTN_NAME, versionName: String = XTRAS_SQLITE_VERSION_NAME,

  gitURL: String = "https://github.com/sqlite/sqlite.git",
  commit: String = XTRAS_SQLITE_COMMIT,
  config: LibraryExtension .() -> Unit = {}
): LibraryExtension =
  extensions.findByName(extnName) as? LibraryExtension ?: registerLibraryExtension(extnName) {
    publishingGroup = CORE_PUBLISHING_PACKAGE
    version = versionName

    git(gitURL, commit)

    configure { target ->

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


fun Project.enableSqliteOld(
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




