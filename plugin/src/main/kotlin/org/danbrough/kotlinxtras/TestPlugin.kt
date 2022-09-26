package org.danbrough.kotlinxtras

import org.gradle.api.Plugin
import org.gradle.api.Project


open class TestPluginExtension {
  var message:String = "default message"
}

class TestPlugin: Plugin<Project> {
  override fun apply(project: Project) {
    println("\"TESTPLUGIN:${project.name} apply()")
    project.extensions.findByType(TestPluginExtension::class.java)?.also {
      println("TESTPLUGIN:${project.name}: FOUND EXIST EXTN: message=${it.message}")
    }
    project.extensions.create("testStuff",TestPluginExtension::class.java).also {
      println("TESTPLUGIN:${project.name}: CREATED TEST EXTN: message = ${it.message}")
    }
  }
}


 fun Project.testPluginTest(){
   project.extensions.findByType(TestPluginExtension::class.java)?.also {
     println("TESTPLUGIN:$name: FOUND EXIST EXTN: message=${it.message}")
   }
 }