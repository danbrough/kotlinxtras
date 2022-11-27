package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.konan.target.KonanTarget



fun LibraryExtension.registerConfigureSourcesTask(target: KonanTarget)=
  project.tasks.create(configureSourcesTaskName(target),Exec::class.java){
    if (!isPackageBuilt(target))
      dependsOn(extractSourcesTaskName(target))
    environment(buildEnvironment(target))
    group = XTRAS_TASK_GROUP
    workingDir(sourcesDir(target))
    configureTask!!(target)
    doFirst {
      println("running $name with: ${commandLine.joinToString(" ")}")
    }
    enabled = !isPackageBuilt(target)
  }


fun LibraryExtension.registerBuildSourcesTask(target: KonanTarget)=
  project.tasks.create(buildSourcesTaskName(target),Exec::class.java){

    group = XTRAS_TASK_GROUP
    environment(buildEnvironment(target))
    workingDir(sourcesDir(target))
    val prefixDir = prefixDir(target)
    outputs.dir(prefixDir)
    enabled = !prefixDir.exists()
    if (enabled)
      configureTask?.also {
        dependsOn(configureSourcesTaskName(target))
      }

    buildTask!!(target)
    installTask?.also {
      finalizedBy(it)
    }

    doLast {
      println("doLast in $name")
    }
  }






