package org.danbrough.xtras.tasks

import org.danbrough.xtras.SHARED_LIBRARY_PATH_NAME
import org.danbrough.xtras.XTRAS_TASK_GROUP
import org.danbrough.xtras.capitalized
import org.danbrough.xtras.decapitalized
import org.danbrough.xtras.library.XtrasLibrary
import org.danbrough.xtras.library.libraryPath
import org.danbrough.xtras.log
import org.danbrough.xtras.xtrasCInteropsDir
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.PrintWriter
import kotlin.collections.plus

typealias CInteropsTargetWriter = List<XtrasLibrary>.(KonanTarget, PrintWriter) -> Unit

data class CInteropsConfig(
  //name of the interops task
  var name: String,

  //package for the interops
  var interopsPackage: String,

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
  var configure: (CInteropsConfig.() -> Unit)? = null,

  //other libraries to be merged into this file
  var dependencies: List<XtrasLibrary> = emptyList(),
)

val defaultCInteropsTargetWriter: CInteropsTargetWriter = { konanTarget, output ->


  val libsDirs = this.map { it.libsDir(konanTarget).absolutePath }
  output.println(
    """
         |compilerOpts.${konanTarget.name} = ${libsDirs.joinToString(separator = " ") { "-I$it/include" }} 
         |linkerOpts.${konanTarget.name} = ${libsDirs.joinToString(separator = " ") { "-L$it/lib" }}
         |libraryPaths.${konanTarget.name} = ${libsDirs.joinToString(separator = " ") { "$it/lib" }}
         |""".trimMargin()
  )

}


fun XtrasLibrary.registerGenerateInteropsTask() {

  val kotlinMultiplatform = project.extensions.findByType<KotlinMultiplatformExtension>() ?: return

  project.log("registerGenerateInteropsTask() for $this")

  val config = CInteropsConfig(
    "xtras${libName.capitalized()}",
    "${project.group}.${libName.decapitalized()}",
    null
  )

  cinteropsConfig?.invoke(config)

  val generateConfig = config.defFile == null
  if (generateConfig)
    config.defFile = project.xtrasCInteropsDir.resolve("xtras_${libName}.def")


  kotlinMultiplatform.apply {

    targets.withType<KotlinNativeTarget> {
      compilations.getByName("main").apply {
        cinterops.create(config.name) {
          defFile(config.defFile!!)
        }
      }

      binaries.withType(Executable::class.java).filter { it.runTask != null }.forEach {
        val env = it.runTask!!.environment
        if (env.containsKey(SHARED_LIBRARY_PATH_NAME))
          env[SHARED_LIBRARY_PATH_NAME] =
            env[SHARED_LIBRARY_PATH_NAME]!!.toString() + File.pathSeparatorChar + libraryPath(konanTarget)
        else
          env[SHARED_LIBRARY_PATH_NAME] = libsDir(konanTarget).resolve("lib")
        project.log("Setting $SHARED_LIBRARY_PATH_NAME for $konanTarget to ${env[SHARED_LIBRARY_PATH_NAME]}")
      }
    }

    project.tasks.withType<KotlinNativeTest> {
      val env = environment.toMutableMap()
      project.log("predefined targets: ${KonanTarget.predefinedTargets.keys.joinToString(",")}")
      val konanTarget = when (targetName){
        "mingwX64" -> KonanTarget.MINGW_X64
        "linuxX64" -> KonanTarget.LINUX_X64
        "linuxArm32Hfp" -> KonanTarget.LINUX_ARM32_HFP
        "linuxArm64" -> KonanTarget.LINUX_ARM64
        "macosArm64" -> KonanTarget.MACOS_ARM64
        "macosX64" -> KonanTarget.MACOS_X64
        "iosArm64" -> KonanTarget.IOS_ARM64
        "iosX64" -> KonanTarget.IOS_X64
        "watchosArm64" -> KonanTarget.WATCHOS_ARM64
        "watchosX64" -> KonanTarget.WATCHOS_X64
        "androidNativeArm64" -> KonanTarget.ANDROID_ARM64
        "androidNativeArm32" -> KonanTarget.ANDROID_ARM32
        "androidNativeX86" -> KonanTarget.ANDROID_X86
        "androidNativeX64" -> KonanTarget.ANDROID_X64
        else -> error("Unhandled targetName: $targetName")
      }
      if (env.containsKey(SHARED_LIBRARY_PATH_NAME))
        environment(SHARED_LIBRARY_PATH_NAME,
          env[SHARED_LIBRARY_PATH_NAME]!!.toString() + File.pathSeparatorChar + libraryPath(konanTarget))
      else
        environment(SHARED_LIBRARY_PATH_NAME,libraryPath(konanTarget))

    }
  }


  project.tasks.withType(CInteropProcess::class.java) {
    libraryDeps.map { it.extractArchiveTaskName(konanTarget) }.forEach {
      //println("adding dependency on $it for $name")
      dependsOn(it)
    }
    dependsOn(extractArchiveTaskName(konanTarget))

    dependsOn(generateInteropsTaskName())
  }

  if (generateConfig)
    project.tasks.register(generateInteropsTaskName()) {
      group = XTRAS_TASK_GROUP

      if (config.headerFile != null && config.headers != null)
        throw Error("Only one of headersFile or headers should be specified for the cinterops config")

      config.headers?.also { headers ->
        inputs.property("headers", headers)
      } ?: config.headerFile?.also { inputs.file(it) }

      inputs.property("targets", supportedTargets)

      outputs.file(config.defFile!!)

      //dependsOn(provideAllBinariesTaskName())

      actions.add {

        config.defFile!!.printWriter().use { output ->
          //write the package
          output.println("package = ${config.interopsPackage}")
          output.println()

          //write the headers
          (config.headers ?: config.headerFile?.readText())?.also {
            output.println(it)
          }

          val extensions = mutableListOf(this@registerGenerateInteropsTask).also {
            it.addAll(config.dependencies)
          }
          supportedTargets.forEach { konanTarget ->
            config.writeTarget(extensions, konanTarget, output)
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

