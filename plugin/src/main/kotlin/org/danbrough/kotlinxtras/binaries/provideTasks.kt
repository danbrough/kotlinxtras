package org.danbrough.kotlinxtras.binaries

import org.jetbrains.kotlin.konan.target.KonanTarget

fun BinaryExtension.registerProvideBinariesTask(target: KonanTarget){
  project.tasks.register(provideBinariesTaskName(target)){
    group = XTRAS_TASK_GROUP
    if (!isPackageBuilt(target))
      dependsOn(buildSourcesTaskName(target))
    onlyIf { !isPackageBuilt(target) }
  }
}