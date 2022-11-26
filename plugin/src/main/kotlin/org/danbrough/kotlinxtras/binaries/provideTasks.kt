package org.danbrough.kotlinxtras.binaries

import org.jetbrains.kotlin.konan.target.KonanTarget

fun LibraryExtension.registerProvideBinariesTask(target: KonanTarget){
  project.tasks.register(provideBinariesTaskName(target)){
    group = XTRAS_TASK_GROUP
    buildTask?.also {
      dependsOn(buildSourcesTaskName(target))
    }
  }
}