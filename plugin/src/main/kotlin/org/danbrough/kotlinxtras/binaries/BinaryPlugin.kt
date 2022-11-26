package org.danbrough.kotlinxtras.binaries

import org.gradle.api.Plugin
import org.gradle.api.Project

open class BinaryConfigurationExtension {
  var gitBinary: String = "/usr/gin/git"
  var wgetBinary: String = "/usr/bin/wget"
  var tarBinary: String = "/usr/bin/tar"
  var autoreconfBinary: String = "/usr/bin/autoreconf"
  var makeBinary: String = "/usr/bin/make"

}

class BinaryPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    println("Binary Plugin initialized")
    target.extensions.create("xtrasBinaries",BinaryConfigurationExtension::class.java)
  }
}