package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.XTRAS_TASK_GROUP
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasPackagesDir
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Exec
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.KonanTarget

fun LibraryExtension.registerPackageTask(target: KonanTarget) =
  project.tasks.register(packageTaskName(target), Exec::class.java) {
    group = XTRAS_TASK_GROUP
    description = "Archives the built package into the packages directory"
    enabled = buildEnabled
    dependsOn(buildSourcesTaskName(target))
    val outputFile = project.xtrasPackagesDir.resolve(packageFile(target))
    workingDir(buildDir(target))
    outputs.file(outputFile)
    commandLine(binaries.tarBinary, "-f", outputFile, "-cpz", "--exclude=share", "./")
  }


/*fun LibraryExtension.registerCopyPackageToLibsTask(target: KonanTarget) =
  project.tasks.register(
    "xtrasExtractPackageToLibs${libName.capitalized()}${target.platformName.capitalized()}",
    Exec::class.java
  ) {
    group = XTRAS_TASK_GROUP
    description = "Extracts the packaged archive to the LibraryExtension.libsDir"
    enabled = buildEnabled
    dependsOn(packageTaskName(target))
    val outputFile = project.xtrasPackagesDir.resolve(packageFile(target))
    workingDir(libsDir(target))
    outputs.dir(libsDir(target))
    commandLine(binaries.tarBinary, "-f", outputFile, "-xpz", "./")
  }*/

fun LibraryExtension.registerPublishingTask(target: KonanTarget) {
  project.logger.info("LibraryExtension.registerPublishingTask: $target group:$publishingGroup version:$version")
  if (!buildEnabled) return
  val packageTask = registerPackageTask(target)
  project.extensions.getByType(PublishingExtension::class.java).apply {
    publications.register(
      "$libName${target.platformName.capitalized()}",
      MavenPublication::class.java
    ) {
      artifactId = name
      groupId = this@registerPublishingTask.publishingGroup
      version = this@registerPublishingTask.version
      artifact(packageTask)
    }
  }




}

