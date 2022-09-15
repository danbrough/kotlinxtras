@file:Suppress("PropertyName")

package org.danbrough.kotlinxtras

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project


open class ProjectProperties {
  var message: String = "default message"

  var SDK_VERSION = 33
  var MIN_SDK_VERSION = 23
  var BUILD_TOOLS_VERSION = "33.0.0"
  var JAVA_VERSION = JavaVersion.VERSION_11
  var KOTLIN_JVM_VERSION = "11"
}


class PropertiesPlugin : Plugin<Project> {

  override fun apply(project: Project) {

    val props = project.extensions.create("projectProperties", ProjectProperties::class.java)

    project.task("helloProperties") {
      it.doLast {
        println("Hello from the PropertiesPlugin! the message is ${props.message} ")
      }
    }
  }


}