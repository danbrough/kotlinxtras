package org.danbrough.kotlinxtras

import org.gradle.api.Project


val Project.runningInIDE: Boolean
  get() = (System.getProperty("idea.active") != null).also {
    project.log("RUNNING IN IDE: $it")
  }