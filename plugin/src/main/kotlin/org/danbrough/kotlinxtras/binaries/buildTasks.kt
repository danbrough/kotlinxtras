package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.konanDepsTaskName
import org.danbrough.kotlinxtras.log
import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.konan.target.KonanTarget


private fun LibraryExtension.registerConfigureSourcesTask(target: KonanTarget) =
  project.tasks.register(configureSourcesTaskName(target), Exec::class.java) {
    dependsOn(target.konanDepsTaskName)
    dependsOn(extractSourcesTaskName(target))
    environment(buildEnvironment(target))
    group = XTRAS_TASK_GROUP
    workingDir(sourcesDir(target))
    doFirst {
      project.log("running $name with: ${commandLine.joinToString(" ")}")
    }
    configureTask!!(target)
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


    val srcDir = sourcesDir(target)
    workingDir(srcDir)

    outputs.dir(buildDir(target))



    project.tasks.findByName(extractSourcesTaskName(target))?.also {
      dependsOn(it)
    }

    configureTask?.also {
      dependsOn(configureSourcesTaskName(target))
    }


    buildTask!!(target)

  }
}







