@file:Suppress("MemberVisibilityCanBePrivate")

package org.danbrough.kotlinxtras

import org.danbrough.kotlinxtras.library.XtrasLibrary
import org.jetbrains.kotlin.konan.target.KonanTarget


class BuildEnvironment(library: XtrasLibrary) {

  inner class Binaries {
    var git = "git"
    var tar = "tar"
  }

  init {
    library.project.log("created BuildEnvironment for $library")
  }

  val binaries = Binaries()

  @XtrasDSLMarker
  var environment: MutableMap<String, String>.() -> Unit = {
    put("PATH", "/bin:/usr/bin:/usr/local/bin")
  }

  @XtrasDSLMarker
  var environmentForTarget: MutableMap<String, String>.(KonanTarget) -> Unit = {}

  fun getEnvironment(target: KonanTarget? = null) = buildMap {
    environment()
    if (target != null)
      environmentForTarget(target)
  }

  @XtrasDSLMarker
  fun binaries(config: Binaries.() -> Unit) {
    binaries.config()
  }
}


