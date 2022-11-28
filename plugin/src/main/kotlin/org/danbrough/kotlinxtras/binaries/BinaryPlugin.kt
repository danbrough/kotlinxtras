package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.*
import org.gradle.api.Plugin
import org.gradle.api.Project

open class BinaryConfigurationExtension {
  var gitBinary: String = "/usr/bin/git"
  var wgetBinary: String = "/usr/bin/wget"
  var tarBinary: String = "/usr/bin/tar"
  var autoreconfBinary: String = "/usr/bin/autoreconf"
  var makeBinary: String = "/usr/bin/make"
}

const val XTRAS_BINARIES_EXTN_NAME = "xtrasBinaries"

class BinaryPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.extensions.create(XTRAS_BINARIES_EXTN_NAME, BinaryConfigurationExtension::class.java)
      .apply {

        gitBinary = target.properties["xtras.bin.git"]?.toString() ?: "/usr/bin/git"
        wgetBinary = target.properties["xtras.bin.wget"]?.toString() ?: "/usr/bin/wget"
        tarBinary = target.properties["xtras.bin.tar"]?.toString() ?: "/usr/bin/tar"
        autoreconfBinary =
          target.properties["xtras.bin.autoreconf"]?.toString() ?: "/usr/bin/autoreconf"
        makeBinary = target.properties["xtras.bin.make"]?.toString() ?: "/usr/bin/make"

        target.tasks.register("xtrasConfig") {
          group = XTRAS_TASK_GROUP
          description = "Prints out the xtras configuration details"
          doFirst {
            println(
              """|Properties:
            |$PROPERTY_XTRAS_DIR=${project.xtrasDir}
            |$PROPERTY_LIBS_DIR=${project.xtrasLibsDir}
            |$PROPERTY_DOWNLOADS_DIR=${project.xtrasDownloadsDir}
            |$PROPERTY_PACKAGES_DIR=${project.xtrasPackagesDir}
            |$PROPERTY_DOCS_DIR=${project.xtrasDocsDir}
            |$PROPERTY_CINTEROPS_DIR=${project.xtrasCInteropsDir}
            |""".trimMargin()
            )
          }
        }
      }
  }
}