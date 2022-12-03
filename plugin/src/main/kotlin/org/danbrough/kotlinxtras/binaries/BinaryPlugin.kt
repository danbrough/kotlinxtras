package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.PROPERTY_CINTEROPS_DIR
import org.danbrough.kotlinxtras.PROPERTY_DOCS_DIR
import org.danbrough.kotlinxtras.PROPERTY_DOWNLOADS_DIR
import org.danbrough.kotlinxtras.PROPERTY_LIBS_DIR
import org.danbrough.kotlinxtras.PROPERTY_PACKAGES_DIR
import org.danbrough.kotlinxtras.PROPERTY_XTRAS_DIR
import org.danbrough.kotlinxtras.XTRAS_BINARY_PLUGIN_ID
import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.projectProperty
import org.danbrough.kotlinxtras.xtrasCInteropsDir
import org.danbrough.kotlinxtras.xtrasDir
import org.danbrough.kotlinxtras.xtrasDocsDir
import org.danbrough.kotlinxtras.xtrasDownloadsDir
import org.danbrough.kotlinxtras.xtrasLibsDir
import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.gradle.api.Plugin
import org.gradle.api.Project

open class BinaryExtension {
  var gitBinary: String = "/usr/bin/git"
  var wgetBinary: String = "/usr/bin/wget"
  var tarBinary: String = "/usr/bin/tar"
  var autoreconfBinary: String = "/usr/bin/autoreconf"
  var makeBinary: String = "/usr/bin/make"
  var enableBuildSupportByDefault: Boolean = false
}

const val XTRAS_BINARIES_EXTN_NAME = "xtrasBinaries"

class BinaryPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.extensions.create(XTRAS_BINARIES_EXTN_NAME, BinaryExtension::class.java)
      .apply {

        val binaryPropertyPrefix = "xtras.bin"
        val binaryProperty: (String,String)->String = { exe,defValue->
          target.projectProperty("$binaryPropertyPrefix.$exe",defValue)
        }

        gitBinary = binaryProperty("git","/usr/bin/git")
        wgetBinary = binaryProperty("wget","/usr/bin/wget")
        tarBinary = binaryProperty("tar","/usr/bin/tar")
        autoreconfBinary = binaryProperty("autoreconf","/usr/bin/autoreconf")
        makeBinary =      binaryProperty("make","/usr/bin/make")


        target.tasks.register("xtrasConfig") {
          group = XTRAS_TASK_GROUP
          description = "Prints out the xtras configuration details"

          doFirst {
            println(
              """
                
                Properties:
                  enableBuildSupportByDefault: $enableBuildSupportByDefault
                
                Binaries:
                  $binaryPropertyPrefix.git:            $gitBinary
                  $binaryPropertyPrefix.wget:           $wgetBinary
                  $binaryPropertyPrefix.tar:            $tarBinary
                  $binaryPropertyPrefix.autoreconf:     $autoreconfBinary
                  $binaryPropertyPrefix.make:           $makeBinary                
                
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
    println("Project.binariesExtension::applying $XTRAS_BINARY_PLUGIN_ID")
    project.plugins.apply(XTRAS_BINARY_PLUGIN_ID)
    project.extensions.getByType(BinaryExtension::class.java).also {
      println("Project.binariesExtension::got $XTRAS_BINARY_PLUGIN_ID buildEnabledDefault: ${it.enableBuildSupportByDefault}")
    }
  }