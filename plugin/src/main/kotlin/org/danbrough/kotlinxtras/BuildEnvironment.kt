package org.danbrough.kotlinxtras

import org.danbrough.kotlinxtras.library.XtrasLibrary


class BuildEnvironment(library: XtrasLibrary) {
  init {
    println("created BuildEnvironment for $library")
  }
}