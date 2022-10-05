package org.danbrough.kotlinxtras

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType


open class PropertiesExtension(private val project: Project) {
   var group:String = project.group.toString()
}

val Project.projectProperties:PropertiesExtension
  get() = extensions.getByType()

class PropertiesPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.extensions.create("projectProperties",PropertiesExtension::class.java,target)
  }
}