package org.danbrough.kotlinxtras

import org.gradle.api.Plugin
import org.gradle.api.Project

const val XTRAS_PLUGIN_ID = "org.danbrough.kotlinxtras.xtras"
class XtrasPlugin: Plugin<Project> {
  override fun apply(project: Project) {
    println("RUNNING XtrasPlugin")
  }
}


