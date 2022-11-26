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
    project.tasks.getByName(XTRAS_PROVIDE_ALL_TASK_NAME).dependsOn(this)
    buildTask?.also {
      val buildSourcesTask = project.tasks.getByName(buildSourcesTaskName(target))
      dependsOn(buildSourcesTask)
      outputs.dir(buildSourcesTask.outputs.files.first())
    }
  }
}