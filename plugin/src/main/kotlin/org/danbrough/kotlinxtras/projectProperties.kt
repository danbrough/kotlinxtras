package org.danbrough.kotlinxtras

import org.gradle.api.Project
import java.io.File
import java.net.URI

inline fun <reified T> Project.projectProperty(
  name: String
): T = projectProperty(name,null,false)

inline fun <reified T> Project.projectProperty(
  name: String,defaultValue: T
): T = projectProperty(name,defaultValue,true)

inline fun <reified T> Project.projectProperty(
  name: String,
  defaultValue: T? = null,
  hasDefault: Boolean
): T {
  val value = if (properties.containsKey(name))
    properties[name].toString()
  else {
    if (hasDefault) return defaultValue as T
    else throw Error("Project property $name not declared. Add it to gradle.properties or specify it with \"-P\" on the command line")
  }

  return when (T::class) {
    String::class -> value
    Int::class -> value.toInt()
    Float::class -> value.toFloat()
    Double::class -> value.toDouble()
    Long::class -> value.toLong()
    Boolean::class -> value.toBoolean()
    File::class -> File(value)
    URI::class -> URI.create(value)
    else -> throw Error("Invalid property type: ${T::class}")
  } as T
}


