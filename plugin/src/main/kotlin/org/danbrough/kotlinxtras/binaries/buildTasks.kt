package org.danbrough.kotlinxtras.binaries

import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.konan.target.KonanTarget


@BinariesDSLMarker
fun BinaryExtension.configureSources(task:SourcesTask) {
  configureTask = task
}


fun BinaryExtension.registerConfigureSourcesTask(target: KonanTarget)=
  project.tasks.register(configureSourcesTaskName(target),Exec::class.java){
    dependsOn(extractSourcesTaskName(target))
    environment(buildEnvironment(target))
    group = XTRAS_TASK_GROUP
    workingDir(sourcesDir(target))
    configureTask!!(target)
  }


@BinariesDSLMarker
fun BinaryExtension.buildSources(task:SourcesTask) {
  buildTask = task
}

fun BinaryExtension.registerBuildSourcesTask(target: KonanTarget)=
  project.tasks.register(buildSourcesTaskName(target),Exec::class.java){
    configureTask?.also {
      dependsOn(configureSourcesTaskName(target))
    }
    group = XTRAS_TASK_GROUP
    environment(buildEnvironment(target))
    workingDir(sourcesDir(target))
    buildTask!!(target)
  }



