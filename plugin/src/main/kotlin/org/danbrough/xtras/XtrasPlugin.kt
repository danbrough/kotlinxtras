package org.danbrough.xtras

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.initialization.DefaultSettings

class XtrasPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.log("applying XtrasPlugin to ${project.projectDir.absolutePath}")


    project.afterEvaluate {
      project.log("afterEvaluate in XtrasPlugin at ${project.projectDir.absolutePath}")
    }
  }
}


class XtrasSettingsPlugin : Plugin<DefaultSettings> {
  override fun apply(target: DefaultSettings) {
    println("applying XtrasPluginSettings")
  }
}