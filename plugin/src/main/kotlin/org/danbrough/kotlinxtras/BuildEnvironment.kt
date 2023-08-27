package org.danbrough.kotlinxtras

import org.danbrough.kotlinxtras.library.XtrasLibrary
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.KonanTarget


class BuildEnvironment(library: XtrasLibrary) {

  val defaultEnv = mapOf("PATH" to "")

  inner class Binaries {
    var git = "git"
    var tar = "tar"
  }

  init {
    library.project.log("created BuildEnvironment for $library")
  }

  val binaries = Binaries()

  @XtrasDSLMarker
  fun binaries(config: Binaries.() -> Unit) {
    binaries.config()
  }


}


