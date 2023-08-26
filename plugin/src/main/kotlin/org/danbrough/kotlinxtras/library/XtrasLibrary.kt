package org.danbrough.kotlinxtras.library

import org.danbrough.kotlinxtras.XtrasDSLMarker
import org.danbrough.kotlinxtras.BuildEnvironment
import org.gradle.api.Project
import org.gradle.configurationcache.extensions.capitalized

@Suppress("LeakingThis")
@XtrasDSLMarker
open class XtrasLibrary(val project: Project, val libName: String, val version: String) {
  init {
    println("Created XtrasLibrary for ${project.projectDir.absolutePath} name: $libName version:$version")
  }

  interface SourceConfig

  @XtrasDSLMarker
  var sourceConfig: SourceConfig? = null

  private val buildEnv: BuildEnvironment = BuildEnvironment(this)

  @XtrasDSLMarker
  fun buildEnvironment(conf: BuildEnvironment.() -> Unit) {
    buildEnv.conf()
  }

  fun taskDownloadSources() = "xtrasDownloadSources${libName.capitalized()}"
}

@XtrasDSLMarker
fun Project.xtrasCreateLibrary(
  libName: String,
  version: String,
  configure: XtrasLibrary.() -> Unit = {}
) = extensions.create(libName, XtrasLibrary::class.java, this, libName, version).apply {

  configure()
  afterEvaluate {
    this@apply.registerTasks()
  }
}


internal fun XtrasLibrary.registerTasks() {
  println("register tasks: $libName")
}

