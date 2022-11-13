package org.danbrough.kotlinxtras

import org.gradle.api.Plugin
import org.gradle.api.Project

open class BinaryPlugin<T : BinaryExtension>(
  private val extnName: String, private val extnClass: Class<T>
) : Plugin<Project> {
  override fun apply(target: Project) {
    val extn = target.extensions.create(extnName, extnClass, target, extnName, DefaultBuildEnv)
      target.afterEvaluate {
        extn.source?.invoke()
      }
    }
  }



