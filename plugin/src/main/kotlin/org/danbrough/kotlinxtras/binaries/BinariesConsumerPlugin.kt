package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.platformName
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.AbstractKotlinNativeCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

typealias BinaryArtifact = BinaryDep.(KonanTarget) -> String?

//maps the BinaryDep and konan target to a dependency notation
val defaultArtifactMap: BinaryArtifact = {
  "$group.$name.binaries:$name${it.platformName.capitalized()}:$version"
}

data class BinaryDep(
  val group: String,
  val name: String,
  val version: String,
  val artifactMap: BinaryArtifact = defaultArtifactMap
) {
  fun binariesTaskName(konanTarget: KonanTarget): String =
    "binaries${name.capitalized()}${konanTarget.platformName.capitalized()}"
}

open class BinariesConsumerExtension(private val project: Project) {
  @Suppress("MemberVisibilityCanBePrivate")
  val dependencies = mutableListOf<BinaryDep>()

  var libsDir: File = project.buildDir.resolve("kotlinxtras")

  fun dependency(
    group: String,
    name: String,
    version: String,
    artifactMap: BinaryArtifact = defaultArtifactMap
  ) = dependencies.add(BinaryDep(group, name, version, artifactMap))


  fun defineBinariesTask(dep: BinaryDep, konanTarget: KonanTarget): TaskProvider<*> {
    val binariesTaskName = dep.binariesTaskName(konanTarget)
    val binaryArtifact = dep.artifactMap.invoke(dep, konanTarget)!!
    project.logger.info("Defining $binariesTaskName")

    val resolveBinariesTask =
      project.tasks.register("resolve${dep.name.capitalized()}Binaries${konanTarget.platformName.capitalized()}") {
        val binaries =
          project.configurations.create("configuration${dep.name.capitalized()}Binaries${konanTarget.platformName.capitalized()}") {
            isVisible = false
            isTransitive = false
            isCanBeConsumed = false
            isCanBeResolved = true
          }

        project.dependencies {
          binaries(binaryArtifact)
        }

        val archives = binaries.resolve()
        if (archives.size != 1) throw Error("Expecting one file in $archives")
        outputs.files(archives.first())
      }

    val extractBinariesTask =
      project.tasks.register(
        "extract${dep.name.capitalized()}Binaries${konanTarget.platformName.capitalized()}",
        Exec::class.java
      ) {
        dependsOn(resolveBinariesTask)
        val archiveFile = resolveBinariesTask.get().outputs.files.first()
        inputs.file(archiveFile)
        outputs.dir(libsDir.resolve(dep.name).resolve(konanTarget.platformName))
        doFirst {
          project.logger.info("Running $name on $archiveFile")
          if (!libsDir.exists()) libsDir.mkdirs()
        }
        commandLine("tar", "xvpf", archiveFile.absolutePath, "-C", libsDir.absolutePath)
      }

    return project.tasks.register(binariesTaskName) {
      doFirst {
        println("Starting $binariesTaskName")
      }
      dependsOn(extractBinariesTask)
    }
  }
}


/*
      project.configurations.create("binaries${konanTarget.platformName.capitalized()}") {
        isVisible = false
        isTransitive = false
        isCanBeConsumed = false
        isCanBeResolved = true
      }
 */





class BinariesConsumerPlugin : Plugin<Project> {

  override fun apply(targetProject: Project) {
    val extn =
      targetProject.extensions.create(
        "binaries",
        BinariesConsumerExtension::class.java,
        targetProject
      )


    fun Project.defineTasks(konanTarget: KonanTarget) {
      extn.dependencies.forEach { dep ->
        dep.artifactMap.invoke(dep, konanTarget)?.also {
          extn.defineBinariesTask(dep, konanTarget)
        }
      }
    }

    targetProject.afterEvaluate {

      tasks.withType<KotlinNativeCompile>()
        .map { KonanTarget.Companion.predefinedTargets[it.target]!! }
        .distinct().forEach { defineTasks(it) }

      tasks.withType<AbstractKotlinNativeCompile<*, *, *>>().all {
        val konanTarget = KonanTarget.Companion.predefinedTargets[target]!!
        extn.dependencies.forEach { binaryDep ->
          binaryDep.artifactMap.invoke(binaryDep, konanTarget)?.also {
            //binary artifact is available for this dependency and target
            //so make this compile task dependent on it.
            dependsOn(binaryDep.binariesTaskName(konanTarget))
          }
        }
      }

      extensions.findByType(KotlinMultiplatformExtension::class.java)?.apply {
        val envKey = if (HostManager.hostIsMac) "DYLD_LIBRARY_PATH" else "LD_LIBRARY_PATH"
        targets.withType<KotlinNativeTarget>().all {
          binaries.withType<Executable>().all {
            extn.dependencies.joinToString(File.pathSeparator) { buildDir.resolve("kotlinxtras/${it.name}/${konanTarget.platformName}/lib").absolutePath }
              .also {
                project.logger.info("$envKey = $it")
                runTask?.environment(envKey, it)
              }
          }
        }
      }
    }

  }
}