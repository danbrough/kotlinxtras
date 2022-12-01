package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasLibsDir
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

fun LibraryExtension.registerProvideBinariesTask(target: KonanTarget) {

  val provideAllGlobalTaskName = "xtrasProvideAll"

  val provideAllGlobalTask =
    project.tasks.findByName(provideAllGlobalTaskName) ?: project.tasks.create(
      provideAllGlobalTaskName
    ) {
      group = XTRAS_TASK_GROUP
      description = "Provide all binaries from all LibraryExtensions"
    }

  val provideAllFromExtensionTask =
    project.tasks.findByName(provideAllBinariesTaskName()) ?: project.tasks.create(
      provideAllBinariesTaskName()
    ) {
      group = XTRAS_TASK_GROUP
      description = "Provide all binaries from a LibraryExtension"
    }

  provideAllGlobalTask.dependsOn(provideAllFromExtensionTask)

  project.tasks.register(provideBinariesTaskName(target)) {
    group = XTRAS_TASK_GROUP
    description = "Provide all binaries for the $libName LibraryExtension"

    if (buildEnabled && target.family.isAppleFamily == HostManager.hostIsMac){
      if (buildTask == null) throw Error("buildTask not configured for $libName")
      val buildSourcesTask = project.tasks.getByName(buildSourcesTaskName(target))
      dependsOn(buildSourcesTask)
      //println("BUILD SOURCES TASK OUTPUTS: ${buildSourcesTask.outputs.files.files}")
      //outputs.dir(buildSourcesTask.outputs.files.first())
      return@register
    }

    //Download the required binaries from maven

    val binaries =
      project.configurations.create("configuration${libName.capitalized()}Binaries${target.platformName.capitalized()}") {
        isVisible = false
        isTransitive = false
        isCanBeConsumed = false
        isCanBeResolved = true
      }

    project.dependencies {
      binaries("$publishingGroup:$libName${target.platformName.capitalized()}:$version")
    }

    val archives = binaries.resolve()
    if (archives.size != 1) throw Error("Expecting one file in $archives")
    val archive = archives.first()

    val outputDir = project.xtrasLibsDir.resolve("$libName/$version/${target.platformName}")
    outputs.dir(outputDir)

    actions.add{
      project.logger.info("deleting $outputDir")
      outputDir.deleteRecursively()
    }

    actions.add{
      project.logger.info("creating $outputDir")
      outputDir.mkdirs()
    }

    actions.add {
      project.exec {
        project.logger.info("extracting ${archive.absolutePath} to $workingDir")
        workingDir(outputDir)
        commandLine(binaryConfiguration.tarBinary,"xfz",archive.absolutePath)
      }
    }

  }.also {
    provideAllFromExtensionTask.dependsOn(it)
  }
}