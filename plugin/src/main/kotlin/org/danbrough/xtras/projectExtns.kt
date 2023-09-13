package org.danbrough.xtras

import org.gradle.api.Project


val runningInIDE: Boolean
  get() = System.getProperty("idea.active") != null