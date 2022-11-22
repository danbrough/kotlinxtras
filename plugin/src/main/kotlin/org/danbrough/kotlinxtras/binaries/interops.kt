package org.danbrough.kotlinxtras.binaries

import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.konan.target.KonanTarget

fun BinaryExtension.registerInteropsTask(target: KonanTarget) =
  project.tasks.register(interopsTaskName(target), Exec::class.java) {
    group = XTRAS_TASK_GROUP

    buildTask?.also {
      dependsOn(buildSourcesTaskName(target))
    }

//    val outputFile = packagesDir.resolve(packageFile(target))
//    onlyIf {
//      !outputFile.exists()
//    }
//
//    outputs.file(outputFile)
//    commandLine("tar", "-f", outputFile, "-cpz", "--exclude=share", "./")
  }
