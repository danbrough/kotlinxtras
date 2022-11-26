package org.danbrough.kotlinxtras.binaries

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
const val XTRAS_PROVIDE_ALL_TASK_NAME = "xtrasProvideAll"

class BinaryPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.extensions.create(XTRAS_BINARIES_EXTN_NAME, BinaryConfigurationExtension::class.java)
      .apply {
        target.tasks.create(XTRAS_PROVIDE_ALL_TASK_NAME) {
          group = XTRAS_TASK_GROUP
        }
      }
  }
}