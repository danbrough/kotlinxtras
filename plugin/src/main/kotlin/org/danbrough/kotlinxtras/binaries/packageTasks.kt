package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.konan.target.KonanTarget

fun BinaryExtension.registerPackageTask(target: KonanTarget) =
  project.tasks.register(packageTaskName(target), Exec::class.java) {
    buildTask?.also {
      dependsOn(buildSourcesTaskName(target))
    }
    val outputFile = project.xtrasPackagesDir.resolve(packageFile(target))
    onlyIf {
      !outputFile.exists()
    }
    group = XTRAS_TASK_GROUP
    workingDir(prefixDir(target))

    outputs.file(outputFile)
    commandLine("tar", "-f", outputFile, "-cpz", "--exclude=share", "./")
  }
