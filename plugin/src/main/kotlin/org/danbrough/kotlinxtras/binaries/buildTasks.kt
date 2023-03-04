package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.konanDepsTaskName
import org.danbrough.kotlinxtras.log
import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.konan.target.KonanTarget


private fun LibraryExtension.registerConfigureSourcesTask(target: KonanTarget) =
  project.tasks.register(configureSourcesTaskName(target), Exec::class.java) {
    dependsOn(target.konanDepsTaskName)
    if (!isPackageBuilt(target)) dependsOn(extractSourcesTaskName(target))

    environment(buildEnvironment(target))
    group = XTRAS_TASK_GROUP
    workingDir(sourcesDir(target))
    configureTask!!(target)
    doFirst {
      project.log("running $name with: ${commandLine.joinToString(" ")}")
    }
    // enabled = !isPackageBuilt(target)
    onlyIf {
      !isPackageBuilt(target)
    }
  }


fun LibraryExtension.registerBuildTasks(target: KonanTarget) {
  configureTask?.also {
    registerConfigureSourcesTask(target)
  }

  project.tasks.register(buildSourcesTaskName(target), Exec::class.java) {

    group = XTRAS_TASK_GROUP
    environment(buildEnvironment(target))
    dependsOn(target.konanDepsTaskName)

    doFirst {
      project.log("running $name environment: $environment")
    }

    workingDir(sourcesDir(target))
    outputs.dir(buildDir(target))

    onlyIf {
      !isPackageBuilt(target)
    }

    if (!isPackageBuilt(target)) {
      project.tasks.findByName(extractSourcesTaskName(target))?.also {
        dependsOn(it)
      }

      configureTask?.also {
        dependsOn(configureSourcesTaskName(target))
      }
    }

    buildTask!!(target)

    finalizedBy(createArchiveTaskName(target))


  }
}







