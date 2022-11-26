package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.konan.target.KonanTarget

fun LibraryExtension.registerPackageTask(target: KonanTarget) =
  project.tasks.register(packageTaskName(target), Exec::class.java) {
    group = XTRAS_TASK_GROUP
    description = "Archives the built package into the packages directory"
    enabled = !isPackageBuilt(target) && buildTask != null
    //println("PACKAGE $target enabled: $enabled packageBuilt: ${isPackageBuilt(target)}")
    dependsOn(provideBinariesTaskName(target))
    val outputFile = project.xtrasPackagesDir.resolve(packageFile(target))
    workingDir(prefixDir(target))
    outputs.file(outputFile)
    commandLine(binaryConfiguration.tarBinary, "-f", outputFile, "-cpz", "--exclude=share", "./")
  }
