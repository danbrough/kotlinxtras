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
    onlyIf {
      !outputFile.exists()
    }
    commandLine(binaries.tarBinary, "-f", outputFile, "-cpz", "--exclude=share", "./")
    doLast {
      println("$name: created package: $outputFile")
    }
  }

fun LibraryExtension.registerPublishingTask(target: KonanTarget) {
  if (!buildEnabled) return
  project.logger.info("LibraryExtension.registerPublishingTask: $target group:$publishingGroup version:$version")

  project.extensions.findByType(PublishingExtension::class.java)?.apply {
    val packageTask = registerPackageTask(target)
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

