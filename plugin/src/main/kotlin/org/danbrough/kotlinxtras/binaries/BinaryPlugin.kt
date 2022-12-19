package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.*
import org.gradle.api.Plugin
import org.gradle.api.Project

open class BinaryExtension {
  var gitBinary: String = "/usr/bin/git"
  var wgetBinary: String = "/usr/bin/wget"
  var tarBinary: String = "/usr/bin/tar"
  var autoreconfBinary: String = "/usr/bin/autoreconf"
  var makeBinary: String = "/usr/bin/make"
  var cmakeBinary: String = "/usr/bin/cmake"
  var goBinary: String = "/usr/bin/go"

  var libraryExtensions = mutableListOf<LibraryExtension>()
}

const val XTRAS_BINARIES_EXTN_NAME = "xtrasBinaries"

class BinaryPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.extensions.create(XTRAS_BINARIES_EXTN_NAME, BinaryExtension::class.java)
      .apply {
        val binaryPropertyPrefix = "xtras.bin"
        val binaryProperty: (String, String) -> String = { exe, defValue ->
          target.projectProperty("$binaryPropertyPrefix.$exe", defValue)
        }

        gitBinary = binaryProperty("git", gitBinary)
        wgetBinary = binaryProperty("wget", wgetBinary)
        goBinary = binaryProperty("go", goBinary)
        tarBinary = binaryProperty("tar", tarBinary)
        autoreconfBinary = binaryProperty("autoreconf", autoreconfBinary)
        makeBinary = binaryProperty("make", makeBinary)
        cmakeBinary = binaryProperty("cmake", cmakeBinary)


        target.tasks.register("xtrasConfig") {
          group = XTRAS_TASK_GROUP
          description = "Prints out the xtras configuration details"

          doFirst {
            println(
              """
                
                Binaries:
                  $binaryPropertyPrefix.git:            $gitBinary
                  $binaryPropertyPrefix.wget:           $wgetBinary
                  $binaryPropertyPrefix.tar:            $tarBinary
                  $binaryPropertyPrefix.go:             $goBinary
                  $binaryPropertyPrefix.autoreconf:     $autoreconfBinary
                  $binaryPropertyPrefix.make:           $makeBinary
                  $binaryPropertyPrefix.cmake:          $cmakeBinary
                
                Paths:
                  $PROPERTY_XTRAS_DIR:            ${project.xtrasDir}
                  $PROPERTY_LIBS_DIR:       ${project.xtrasLibsDir}
                  $PROPERTY_DOWNLOADS_DIR:  ${project.xtrasDownloadsDir}
                  $PROPERTY_PACKAGES_DIR:   ${project.xtrasPackagesDir}
                  $PROPERTY_DOCS_DIR:       ${project.xtrasDocsDir}
                  $PROPERTY_CINTEROPS_DIR:  ${project.xtrasCInteropsDir}
                """.trimIndent()
            )
          }
        }
      }
  }
}


val Project.binariesExtension: BinaryExtension
  get() = project.extensions.findByType(BinaryExtension::class.java) ?: let {
    project.plugins.apply(XTRAS_BINARY_PLUGIN_ID)
    project.extensions.getByType(BinaryExtension::class.java)
  }