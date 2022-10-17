@file:Suppress("PropertyName", "unused")

package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasTaskGroup
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Exec
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeTest
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.KonanTarget


/**
 * Using the tar command to create/extract archives as gradle Copy task doesn't preserve symlinks.
 * A suggested work-around below but it requires the "ln" command so will assume the tar command is
 * available
 */

/*
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.gradle.api.tasks.Copy

class CopyWithSymlink extends Copy {
  public CopyWithSymlink() {
    super();
    eachFile { details ->
      Path sourcePath = FileSystems.getDefault().getPath(details.file.path)
      if(Files.isSymbolicLink(sourcePath)) {
        details.exclude()
        Path destinationPath = Paths.get("${destinationDir}/${details.relativePath}")
        if(Files.notExists(destinationPath.parent)) {
          project.mkdir destinationPath.parent
        }
        project.exec {
          commandLine 'ln', '-sf', Files.readSymbolicLink(sourcePath), destinationPath
        }
      }
    }
  }
}*/

data class BinDep(val group: String?, val name: String, val version: String?)

open class BinariesExtension(private val project: Project) {
  internal var binDeps: MutableSet<BinDep> = mutableSetOf()

  private val projectGroup = "org.danbrough.kotlinxtras"

  fun enableCurl(version: String = CurrentVersions.curl) {
    binDeps.add(BinDep(projectGroup, "curl", version))
  }

  fun enableOpenSSL(version: String = CurrentVersions.openssl) {
    binDeps.add(BinDep(projectGroup, "openssl", version))
  }

  fun enableSqlite(version: String = CurrentVersions.sqlite) {
    binDeps.add(BinDep(projectGroup, "sqlite", version))
  }
  fun enableIconv(version: String = CurrentVersions.iconv) {
    binDeps.add(BinDep(projectGroup, "iconv", version))
  }
  fun addBinaryDependency(binDep: BinDep) {
    binDeps.add(binDep)
  }

  var taskToPlatformName: (Task) -> String? = { task ->
    when (task) {
      is KotlinNativeTest -> task.targetName!!
      is KotlinNativeCompile -> task.target
      else -> null
    }
  }
}


class BinariesConsumerPlugin : Plugin<Project> {

  override fun apply(targetProject: Project) {

    with(targetProject){
      extensions.create("binaries", BinariesExtension::class.java, targetProject)

      configurations.register("binary") {
        isTransitive = false
      }

      afterEvaluate {
        configureBinaries()
      }
    }

  }

}

private fun Project.configureBinaries() {
  afterEvaluate {

    println("CONFIGURE BINARIES")

    val binaries =
      project.extensions.findByType<BinariesExtension>() ?: return@afterEvaluate

    val mppExtension =
      project.extensions.findByType<KotlinMultiplatformExtension>() ?: return@afterEvaluate

    val konanTargets = mppExtension.targets.withType<KotlinNativeTarget>()
      .map { it.konanTarget }.distinct()

    println("KONANTARGETS: $konanTargets")

    val binaryConfiguration = project.configurations.getByName("binary")

    val localBinariesDir = project.buildDir.resolve("kotlinxtras")

    project.dependencies {
      binaries.binDeps.forEach { binDep ->
        println("BIN DEP: $binDep")
        konanTargets.forEach { target ->
          val binDepLib =
            "${binDep.group}.${binDep.name}.binaries:${binDep.name}${target.platformName.capitalized()}:${binDep.version}"
          project.logger.log(LogLevel.INFO, "Adding binary support with $binDepLib")
          binaryConfiguration(binDepLib)
        }
      }
    }


    binaryConfiguration.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->

      println("RESOLVED ARTIFACT: $artifact")
      project.tasks.register<Exec>("extract${artifact.name.capitalized()}") {
        doFirst {
          println("extracting ${artifact.file} into $localBinariesDir")
          if (!localBinariesDir.exists()) localBinariesDir.mkdirs()
        }
        group = xtrasTaskGroup
//        from(project.tarTree(artifact.file).matching {
//          exclude("**/META-INF")
//          exclude("**/META-INF/*")
//        })
//        into(localBinariesDir)
        commandLine("tar","-xzpf",artifact.file,"-C",localBinariesDir.absolutePath)
      }
    }


    binaries.binDeps.forEach { binDep ->
      project.tasks.withType(KotlinNativeCompile::class).forEach {
        project.logger.log(
          LogLevel.INFO,
          "adding extract dependency on ${binDep.name} for ${it.name}"
        )
        val konanTarget = KonanTarget.predefinedTargets[it.target]!!
        it.dependsOn("extract${binDep.name.capitalized()}${konanTarget.platformName.capitalized()}Binaries")
      }
    }
  }
}
