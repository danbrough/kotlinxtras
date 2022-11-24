package org.danbrough.kotlinxtras.binaries

import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.konan.target.KonanTarget



fun BinaryExtension.registerConfigureSourcesTask(target: KonanTarget)=
  project.tasks.register(configureSourcesTaskName(target),Exec::class.java){
    if (!isPackageBuilt(target))
      dependsOn(extractSourcesTaskName(target))
    environment(buildEnvironment(target))
    group = XTRAS_TASK_GROUP
    workingDir(sourcesDir(target))
    configureTask!!(target)
    doFirst {
      println("running $name with: ${commandLine.joinToString(" ")}")
    }
    onlyIf { !isPackageBuilt(target) }
  }


fun BinaryExtension.registerBuildSourcesTask(target: KonanTarget)=
  project.tasks.register(buildSourcesTaskName(target),Exec::class.java){
    configureTask?.also {
      dependsOn(configureSourcesTaskName(target))
    }
    group = XTRAS_TASK_GROUP
    environment(buildEnvironment(target))
    workingDir(sourcesDir(target))
    val prefixDir = prefixDir(target)
    outputs.dir(prefixDir)

    buildTask!!(target)
    installTask?.also {
      finalizedBy(it)
    }
    onlyIf { !isPackageBuilt(target) }
  }






