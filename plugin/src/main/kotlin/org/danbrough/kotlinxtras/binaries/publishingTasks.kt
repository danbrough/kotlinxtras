package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.platformName
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.konan.target.KonanTarget


fun LibraryExtension.registerPublishingTask(target: KonanTarget) {
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

