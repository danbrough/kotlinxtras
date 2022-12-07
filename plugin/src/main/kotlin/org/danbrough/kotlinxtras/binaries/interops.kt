package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.xtrasCInteropsDir
import org.danbrough.kotlinxtras.xtrasLibsDir
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.PrintWriter

typealias CInteropsTargetWriter = LibraryExtension.(KonanTarget, PrintWriter) -> Unit

data class CInteropsConfig(
  //name of the interops task
  var name: String,
  //path to the generated (or preexisting) def file
  var defFile: File,

  //to be added to the start of the generated interops file
  //no file will be generated if this and [headers] remain null
  var headerFile: File? = null,

  /**
   * Specify the interops headers using a field instead of the [headerFile]
   */
  var headers: String? = null,

  //writes output to the defs file for a konanTarget
  var writeTarget: CInteropsTargetWriter = defaultCInteropsTargetWriter,

  //customize the default config
  var configure: (CInteropsConfig.() -> Unit)? = null
)

val defaultCInteropsTargetWriter: CInteropsTargetWriter = { konanTarget, output ->
  val prefixDir = buildDir(konanTarget).absolutePath
  output.println(
    """
         |compilerOpts.${konanTarget.name} = -I$prefixDir/include 
         |linkerOpts.${konanTarget.name} = -L$prefixDir/lib 
         |libraryPaths.${konanTarget.name} = $prefixDir/lib     
         |""".trimMargin()
  )
}

fun LibraryExtension.registerGenerateInteropsTask() {

  project.logger.info("registerGenerateInteropsTask for $this")

  val config =  CInteropsConfig(
    "xtras${libName.capitalized()}",
    project.xtrasCInteropsDir.resolve("xtras_${libName}.def")
  )

  cinteropsConfigTask?.invoke(config)

  project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {
    val libPathKey = if (HostManager.hostIsMac) "DYLD_LIBRARY_PATH" else "LD_LIBRARY_PATH"
    targets.withType(KotlinNativeTarget::class.java).all {
      compilations.getByName("main").apply {
        cinterops.create(config.name) {
          defFile(config.defFile)
        }
      }

      binaries.withType(Executable::class.java).filter { it.runTask != null }.forEach {
        val env = it.runTask!!.environment
        if (env.containsKey(libPathKey))
          env[libPathKey] = env[libPathKey]!!.toString() + File.pathSeparatorChar +buildDir(konanTarget).resolve("lib")
        else
          env[libPathKey] = buildDir(konanTarget).resolve("lib")
        project.logger.debug("Setting $libPathKey for $konanTarget to ${env[libPathKey]}")
      }
    }
  }

  project.tasks.withType(CInteropProcess::class.java).all {
    dependsOn(generateCInteropsTaskName())
  }

  project.tasks.register(generateCInteropsTaskName()) {
    group = XTRAS_TASK_GROUP

    if (config.headerFile != null && config.headers != null)
      throw Error("Only one of headersFile or headers should be specified for the cinterops config")

    config.headers?.also { headers ->
      inputs.property("headers", headers)
    } ?: inputs.file(config.headerFile!!)

    inputs.property("xtrasLibs",project.xtrasLibsDir)

    val defFile = config.defFile
    outputs.file(defFile)

    dependsOn(provideAllBinariesTaskName())

    actions.add {

      defFile.printWriter().use { output ->
        //write the headers
        output.println(config.headers ?: config.headerFile!!.readText())

        supportedTargets.forEach { konanTarget ->
          config.writeTarget(this@registerGenerateInteropsTask, konanTarget, output)
        }
      }
    }

    doLast {
      println("generated $defFile")
    }
  }
}
