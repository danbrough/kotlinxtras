package org.danbrough.kotlinxtras.tasks

/*
    if (publishBinaries && (HostManager.hostIsMac == target.family.isAppleFamily)) {
      publishing.publications.create(
        "$libName${target.platformName.capitalize()}", MavenPublication::class.java
      ) {
        artifactId = "${libName}${target.platformName.capitalize()}"
        version = this@registerXtrasTasks.version
        artifact(archiveTask)
        groupId = this@registerXtrasTasks.publishingGroup
      }
    }

 */

