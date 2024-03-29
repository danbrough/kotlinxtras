package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.SHARED_LIBRARY_PATH_NAME
import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.xtrasCInteropsDir
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.PrintWriter

typealias CInteropsTargetWriter = LibraryExtension.(KonanTarget, PrintWriter) -> Unit

data class CInteropsConfig(
  //name of the interops task
  var name: String,

  //package for the interops
  var interopsPackage: String? = null,

  //path to the generated (or preexisting) def file
  //if pre-existing then [interopsPackage], [headers] and [headersFile] should not be set
  var defFile: File? = null,

  //to be added to the start of the generated interops file
  //no file will be generated if this and [headers] remain null
  var headerFile: File? = null,

  /**
   * Specify the interops headers instead of the [headerFile]
   */
  var headers: String? = null,

  /**
   * Extra header source code for the headersFile [headerFile]
   */
  var headersSource: String? = null,

  //writes output to the defs file for a konanTarget
  var writeTarget: CInteropsTargetWriter = defaultCInteropsTargetWriter,

  //customize the default config
  var configure: (CInteropsConfig.() -> Unit)? = null
)

val defaultCInteropsTargetWriter: CInteropsTargetWriter = { konanTarget, output ->
  val libsDir = libsDir(konanTarget).absolutePath
  output.println(
    """
         |compilerOpts.${konanTarget.name} = -I$libsDir/include 
         |linkerOpts.${konanTarget.name} = -L$libsDir/lib 
         |libraryPaths.${konanTarget.name} = $libsDir/lib     
         |""".trimMargin()
  )
}

fun LibraryExtension.registerGenerateInteropsTask() {

  project.log("registerGenerateInteropsTask() for $this")


  val config = CInteropsConfig(
    "xtras${libName.capitalized()}",
    "$publishingGroup.$libName",
    null
  )

  cinteropsConfigTasks.forEach {
    it.invoke(config)
  }

  val generateConfig = config.defFile == null
  if (generateConfig)
    config.defFile = project.xtrasCInteropsDir.resolve("xtras_${libName}.def")

  project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {


    targets.withType(KotlinNativeTarget::class.java).all {
      compilations.getByName("main").apply {
        cinterops.create(config.name) {
          defFile(config.defFile!!)
        }
      }

      binaries.withType(Executable::class.java).filter { it.runTask != null }.forEach {
        val env = it.runTask!!.environment
        if (env.containsKey(SHARED_LIBRARY_PATH_NAME))
          env[SHARED_LIBRARY_PATH_NAME] =
            env[SHARED_LIBRARY_PATH_NAME]!!.toString() + File.pathSeparatorChar + libsDir(
              konanTarget
            ).resolve("lib")
        else
          env[SHARED_LIBRARY_PATH_NAME] = libsDir(konanTarget).resolve("lib")
        project.logger.info("Setting $SHARED_LIBRARY_PATH_NAME for $konanTarget to ${env[SHARED_LIBRARY_PATH_NAME]}")
      }
    }
  }

  project.tasks.withType(CInteropProcess::class.java).all {
    dependsOn(generateCInteropsTaskName())
  }

  if (generateConfig)
    project.tasks.register(generateCInteropsTaskName()) {
      group = XTRAS_TASK_GROUP

      if (config.headerFile != null && config.headers != null)
        throw Error("Only one of headersFile or headers should be specified for the cinterops config")

      config.headers?.also { headers ->
        inputs.property("headers", headers)
      } ?: config.headerFile?.also { inputs.file(it) }

      inputs.property("targets", supportedBuildTargets)

      outputs.file(config.defFile!!)

      //dependsOn(provideAllBinariesTaskName())

      actions.add {

        config.defFile!!.printWriter().use { output ->
          //write the package
          output.println("package = ${config.interopsPackage}")
          //write the headers
          (config.headers ?: config.headerFile?.readText())?.also {
            output.println(it)
          }

          supportedTargets.forEach { konanTarget ->
            config.writeTarget(this@registerGenerateInteropsTask, konanTarget, output)
          }

          if (config.headersSource != null) {
            output.println("---")
            output.println(config.headersSource)
          }
        }
      }



      doLast {
        println("generated ${config.defFile}")
      }
    }
}
