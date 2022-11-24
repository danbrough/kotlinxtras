package org.danbrough.kotlinxtras.binaries

import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.DefaultCInteropSettings
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.PrintWriter

typealias CInteropsTargetWriter = BinaryExtension.(KonanTarget, PrintWriter)->Unit

data class CInteropsConfig(
  //name of the interops task
  var name:String,
  //path to the generated (or preexisting) def file
  var defFile: File,
  //to be added to the start of the generated interops file
  //no file will be generated if this remains null
  var headersFile: File? = null,

  //writes output to the defs file for a konanTarget
  var writeTarget: CInteropsTargetWriter  = defaultCInteropsTargetWriter,

  //customize the default config
  var configure: (DefaultCInteropSettings.()->Unit)? = null
)

val defaultCInteropsTargetWriter: CInteropsTargetWriter = { konanTarget,output->
  val prefixDir = prefixDir(konanTarget).absolutePath
  output.println(
    """
         |compilerOpts.${konanTarget.name} = -I$prefixDir/include 
         |linkerOpts.${konanTarget.name} = -L$prefixDir/lib 
         |libraryPaths.${konanTarget.name} = $prefixDir/lib     
         |""".trimMargin()
  )
}

fun BinaryExtension.registerGenerateInteropsTask() {

  val config = CInteropsConfig("xtras${libName.capitalized()}",project.file("src/cinterops/xtras_${libName}.def"))
  cinteropsConfigTask?.invoke(config)

  //register empty task if no headers are provided
  config.headersFile ?:  project.tasks.register(generateCInteropsTaskName()) {
    group = XTRAS_TASK_GROUP
    doFirst{
      println("not generating ${config.defFile} as no headers provided.")
    }
  }

  project.extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply{
    targets.withType(KotlinNativeTarget::class.java).all {
      compilations.getByName("main").apply {
        cinterops.create(config.name){
          defFile(config.defFile)
          config.configure?.invoke(this)
        }
      }
    }
  }

  project.tasks.withType(CInteropProcess::class.java).all {
    dependsOn(generateCInteropsTaskName())
  }

  project.tasks.register(generateCInteropsTaskName()) {
    group = XTRAS_TASK_GROUP
    val headersFile = config.headersFile!!
    inputs.file(headersFile)
    val defFile = config.defFile
    outputs.file(defFile)

    dependsOn(konanTargets.map { provideBinariesTaskName(it) })

    actions.add {

      defFile.printWriter().use { output ->
        output.println(headersFile.readText())

        konanTargets.forEach { konanTarget ->
          config.writeTarget(this@registerGenerateInteropsTask, konanTarget, output)
        }
      }
    }

    doLast {
      println("generated $defFile from $headersFile")
    }
  }
}
