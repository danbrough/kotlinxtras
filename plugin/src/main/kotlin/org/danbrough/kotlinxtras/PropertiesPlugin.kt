package org.danbrough.kotlinxtras

import org.gradle.api.Plugin
import org.gradle.api.Project



open class PropertiesExtension {
  var message:String = "Hello from properties extension"
  internal fun initialize(project: Project){

  }
}

class PropertiesPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.extensions.create("properties",PropertiesExtension::class.java).initialize(target)
  }
}