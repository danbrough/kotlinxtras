package org.danbrough.kotlinxtras

import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import java.io.File

const val KOTLIN_XTRAS_DIR_NAME = "kotlinxtras"
const val XTRAS_TASK_GROUP = "xtras"


interface BuildEnv {
  var gitBinary: String
  var wgetBinary: String
  var tarBinary: String
}

typealias DownloadSourcesTask = TaskProvider<*>

object DefaultBuildEnv : BuildEnv {
  override var gitBinary: String = "/usr/bin/git"
  override var wgetBinary: String = "/usr/bin/wget"
  override var tarBinary: String = "/usr/bin/tar"
}

open class BinaryExtension(
  val project: Project,
//Unique identifier for a binary package
  var libName: String, var buildEnvironment: BuildEnv
) {

  open var version: String = "unspecified"

  open fun xtrasDir(): File = project.rootProject.buildDir.resolve(KOTLIN_XTRAS_DIR_NAME)

  open fun sourcesDir(): File = xtrasDir().resolve("src/$libName/$version")

  open fun buildDir(): File = xtrasDir().resolve("$libName/$version")

  open fun downloadsDir(): File = xtrasDir().resolve("downloads")

  open  var source: SourceProvider? = null

}

