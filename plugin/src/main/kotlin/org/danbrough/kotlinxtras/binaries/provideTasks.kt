package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasLibsDir
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.konan.target.KonanTarget

fun LibraryExtension.registerProvideBinariesTask(target: KonanTarget) =
  project.tasks.register(provideBinariesTaskName(target)) {
    group = XTRAS_TASK_GROUP
    description = "Provide all binaries for the $libName LibraryExtension"

/*    if (buildEnabled && target.family.isAppleFamily == HostManager.hostIsMac) {
      if (buildTask == null) throw Error("buildTask not configured for $libName")
      dependsOn(packageTaskName(target))
      return@register
    }*/

    //Download the prebuilt binaries from maven

    val binariesConfiguration =
      project.configurations.create("configuration${libName.capitalized()}Binaries${target.platformName.capitalized()}") {
        isVisible = false
        isTransitive = false
        isCanBeConsumed = false
        isCanBeResolved = true
      }

    project.dependencies {
      binariesConfiguration("$publishingGroup:$libName${target.platformName.capitalized()}:$version")
    }

    val outputDir = project.xtrasLibsDir.resolve("$libName/$version/${target.platformName}")
    outputs.dir(outputDir)

    actions.add {
      project.logger.info("deleting $outputDir")
      outputDir.deleteRecursively()
    }

    actions.add {
      project.logger.info("creating $outputDir")
      outputDir.mkdirs()
    }

    actions.add {
      project.exec {
        //println("BINARIES FILES: ${binariesConfiguration.files}")
        val archives = binariesConfiguration.resolve()
        if (archives.size != 1) throw Error("Expecting one file in $archives")
        val archive = archives.first()

        project.logger.info("extracting ${archive.absolutePath} to $workingDir")
        workingDir(outputDir)
        commandLine(binaries.tarBinary, "xfz", archive.absolutePath)
      }
    }

  }
