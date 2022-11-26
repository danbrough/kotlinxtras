package org.danbrough.kotlinxtras.binaries

import org.jetbrains.kotlin.konan.target.KonanTarget

fun LibraryExtension.registerProvideBinariesTask(target: KonanTarget) {
  val provideAllTask =
    project.tasks.findByName(provideAllBinariesTaskName()) ?: project.tasks.create(
      provideAllBinariesTaskName()
    ) {
      group = XTRAS_TASK_GROUP
    }

  project.tasks.create(provideBinariesTaskName(target)) {
    group = XTRAS_TASK_GROUP
    provideAllTask.dependsOn(this)
    buildTask?.also {
      dependsOn(buildSourcesTaskName(target))
    }
  }
}