@file:Suppress("MemberVisibilityCanBePrivate")

package org.danbrough.kotlinxtras

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.io.FileReader
import java.net.URI
import java.util.Properties
import kotlin.reflect.KProperty

object ProjectProperties : Plugin<Project>{
  
  
  const val SDK_VERSION = 33
  const val MIN_SDK_VERSION = 23
  const val BUILD_TOOLS_VERSION = "33.0.0"
  val JAVA_VERSION = JavaVersion.VERSION_11
  const val KOTLIN_JVM_VERSION = "11"
  
  val IDE_ACTIVE: Boolean
    get() = System.getProperty("idea.active", "false").toBoolean()
  
  lateinit var LOCAL_M2: URI
  
  
  private val snapshotVersionFormat: String by createProperty(
    "build.snapshot.format",
    "0.0.1-SHAPSHOT"
  )
  
  private val releaseVersionFormat: String by createProperty(
    "build.version.format",
    "0.0.1-alpha%02d"
  )
  
  private val buildVersionFormat: String
    get() = if (buildSnapshot) snapshotVersionFormat else releaseVersionFormat
  
  val buildVersion: Int by createProperty("build.version", "0")
  
  val projectGroup: String by createProperty("project.group", "org.danbrough.kotlinxtras")
  
  val buildSnapshot: Boolean by createProperty(
    "build.snapshot", "false"
  )
  
  private val buildVersionOffset: Int by createProperty(
    "build.version.offset", "0"
  )
  
  val buildVersionName: String
    get() = buildVersionName()
  
  private fun buildVersionName(version: Int = buildVersion) =
    buildVersionFormat.format(version - buildVersionOffset)
  
  val localProperties = Properties()
  var projectProperties = Properties()
  
  class ProjectProperty(
    val key: String,
    val hasDefault: Boolean = true,
    val defaultValue: String?
  ) {
    inline operator fun <reified T : Any?> getValue(thisRef: Any?, property: KProperty<*>): T =
      getProjectProperty(key, defaultValue, hasDefault)
  }
  
  fun createProperty(propName: String) =
    ProjectProperty(propName, false, null)
  
  fun createProperty(propName: String, defaultValue: String?, hasDefault: Boolean = true) =
    ProjectProperty(propName, hasDefault, defaultValue)
  
  @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
  inline fun <reified T : Any?> getProjectProperty(
    propName: String,
    defaultValue: String?,
    hasDefault: Boolean = true,
  ): T {

    val value = projectProperties["override.$propName"] ?: localProperties[propName] ?: projectProperties[propName] ?: let {
      if (!hasDefault) throw Error("Property $propName not found")
      defaultValue
    }
    
    value as String
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
  
  private fun init(project: Project) {
    
    LOCAL_M2 = project.buildDir.resolve("m2").toURI()
    localProperties.clear()
    
    project.rootProject.file("local.properties").also { localPropsFile ->
      if (localPropsFile.exists()) {
        FileReader(localPropsFile).use {
          localProperties.load(it)
        }
      }
    }
    
    projectProperties.clear()
    project.rootProject.properties.entries.forEach {
      // println("setting ${it.key} -> ${it.value}")
      if (it.value != null)
        projectProperties[it.key] = it.value
    }
    
    registerTasks(project)
  }
  
  
  private fun registerTasks(project: Project) {
    
    project.tasks.create("buildVersion") {
      doLast {
        println(buildVersion)
      }
    }
    
    project.tasks.create("buildVersionName") {
      doLast {
        println(buildVersionName)
      }
    }
    
    project.tasks.create("buildVersionNameNext") {
      doLast {
        println(buildVersionName(buildVersion + 1))
      }
    }
    
    project.tasks.create("buildVersionIncrement") {
      doLast {
        project.rootProject.file("gradle.properties").readLines().map {
          if (it.contains("build.version=")) "build.version=${buildVersion + 1}"
          else it
        }.also { lines ->
          project.rootProject.file("gradle.properties").writer().use { writer ->
            lines.forEach {
              writer.write("$it\n")
            }
          }
        }
        println(buildVersionName(buildVersion + 1))
      }
    }
  }

  override fun apply(target: Project) {
    init(target)
  }

}



