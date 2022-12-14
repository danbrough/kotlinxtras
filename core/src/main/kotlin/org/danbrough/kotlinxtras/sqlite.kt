@file:Suppress("unused")

package org.danbrough.kotlinxtras


import org.danbrough.kotlinxtras.binaries.*
import org.gradle.api.Project

const val XTRAS_SQLITE_EXTN_NAME = "sqlite"


fun Project.enableSqlite(name: String = XTRAS_SQLITE_EXTN_NAME,config:LibraryExtension.()->Unit = {}): LibraryExtension =

  registerLibraryExtension(name) {
    publishingGroup = CORE_PUBLISHING_PACKAGE
    version = "3.40.0"

    download("https://www.sqlite.org/2022/sqlite-autoconf-3400000.tar.gz") {
      stripTopDir = true
      tarExtractOptions = "xfz"
    }

    // git("https://github.com/sqlite/sqlite.git","2f2c5e2061cfebfad6b9aca4950d960caec073d8")

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




