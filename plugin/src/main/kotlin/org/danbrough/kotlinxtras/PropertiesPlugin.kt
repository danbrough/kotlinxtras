@file:Suppress("MemberVisibilityCanBePrivate")

package org.danbrough.kotlinxtras

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.kotlin.dsl.getByType
import java.io.File
import java.net.URI
import kotlin.reflect.KProperty


open class PropertiesExtension( val project: Project) {
  var group: String = project.group.toString()
  val buildVersionFormat: String by project.properties
  val message:String by project.createProjectProperty("message","default message")


}


class  ProjectProperty(val project: Project,val propertyName:String,val defValue: Any?){
  inline operator fun <reified T:Any?> getValue(thisRef: Any?, property: KProperty<*>): T =
    project.projectProperty(propertyName, defValue) as T
}
fun  Project.createProjectProperty(propertyName:String,defValue:Any) = ProjectProperty(project,propertyName,defValue)
inline fun <reified T> Project.projectProperty(name: String, defValue: T): T {
  val ext = extensions.getByType<ExtraPropertiesExtension>()
  if (!ext.has(name)) return defValue
  val value = ext.get(name)?.toString()
  return when (T::class) {
    String::class -> value
    Long::class -> value?.toLong()
    Int::class -> value?.toInt()
    Double::class -> value?.toDouble()
    Float::class -> value?.toFloat()
    Boolean::class -> value?.toBoolean()
    File::class -> value?.let { File(it)}
    URI::class -> URI.create(value)
    else -> TODO("Support ${T::class}")
  } as T
}



val Project.projectProperties: PropertiesExtension
  get() = extensions.getByType()

class PropertiesPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.extensions.create("projectProperties", PropertiesExtension::class.java, target)
  }
}