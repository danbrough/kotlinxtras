package org.danbrough.kotlinxtras.binaries


import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.KonanTarget

fun LibraryExtension.registerPackageTask(target: KonanTarget): TaskProvider<Exec> {

  val packageAllTaskName = "xtrasPackageAll"

  val packageAllTask =
    project.tasks.findByName(packageAllTaskName) ?: project.tasks.create(packageAllTaskName) {
      group = XTRAS_TASK_GROUP
      description = "Package all targets in project"
    }

  val packageAllInLibraryTaskName = "xtrasPackage${libName.capitalized()}"

  val packageAllInLibraryTask =
    project.tasks.findByName(packageAllInLibraryTaskName) ?: project.tasks.create(
      packageAllInLibraryTaskName
    ) {
      group = XTRAS_TASK_GROUP
      description = "Package all targets for $libName"
    }.also {
      packageAllTask.dependsOn(it)
    }

  val cleanUpTaskName = "xtrasClean${libName.capitalized()}${target.platformName.capitalized()}"
  project.tasks.register(cleanUpTaskName){
    doFirst {
      println("$name: Deleting src and build directories")
      buildDir(target).deleteRecursively()
      sourcesDir(target).deleteRecursively()
    }
  }

  return project.tasks.register(packageTaskName(target), Exec::class.java) {
    group = XTRAS_TASK_GROUP
    description = "Archives the built package into the packages directory"
    dependsOn(buildSourcesTaskName(target))
    val outputFile = project.xtrasPackagesDir.resolve(packageFileName(target))
    workingDir(buildDir(target))
    outputs.file(outputFile)
    onlyIf {
      !outputFile.exists()
    }

    commandLine(binaries.tarBinary, "-f", outputFile, "-cpz", "--exclude=share", "./")
    doLast {
      val result = executionResult.get()
      println("$name: created package: $outputFile result: $result")
    }


    finalizedBy("publish${libName.capitalized()}${target.platformName.capitalized()}PublicationToXtrasRepository",cleanUpTaskName)
  }.also {
    packageAllInLibraryTask.dependsOn(it)
  }
}