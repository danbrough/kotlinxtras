package org.danbrough.kotlinxtras.binaries

import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.konan.target.KonanTarget

fun BinaryExtension.registerPackageTask(target: KonanTarget) =
  project.tasks.register(packageTaskName(target), Exec::class.java) {
    println("REGISTERING PACKAGE TASK: $name")
    buildTask?.also {
      dependsOn(buildSourcesTaskName(target))
    }
    group = XTRAS_TASK_GROUP
    workingDir(prefixDir(target))
    val packageFile = "${libName}_${this@registerPackageTask.version}.tar.gz"
    val outputFile = packagesDir.resolve(packageFile)
    outputs.file(outputFile)
    commandLine("tar", "-f", outputFile, "-cpz", "--exclude=share", "./")
  }
