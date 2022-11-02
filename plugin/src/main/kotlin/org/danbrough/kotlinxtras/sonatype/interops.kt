package org.danbrough.kotlinxtras.sonatype

import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasTaskGroup
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager.Companion.hostIsMac
import java.io.File

private const val defaultInteropsDefTaskName = "generateCInteropsDef"

fun Project.generateInterops(
  libName: String,
  headerFile: File,
  outputFile: File,
  interopsTaskName: String = defaultInteropsDefTaskName
): TaskProvider<*> {

  val kotlin = extensions.getByType<KotlinMultiplatformExtension>()

  tasks.withType<org.jetbrains.kotlin.gradle.tasks.CInteropProcess>() {
    dependsOn(interopsTaskName)
    if (hostIsMac == konanTarget.family.isAppleFamily)
      dependsOn("build${konanTarget.platformName.capitalized()}")
  }

  return tasks.register(interopsTaskName) {
    inputs.file(headerFile)
    outputs.file(outputFile)
    group = xtrasTaskGroup
    doFirst {
      val outputFile = outputs.files.files.first()
      println("Generating $outputFile")

      outputFile.printWriter().use { output ->
        output.println(inputs.files.files.first().readText())

        kotlin.targets.withType<KotlinNativeTarget>().forEach {
          val konanTarget = it.konanTarget
          output.println(
            """
         |compilerOpts.${konanTarget.name} = -Ibuild/kotlinxtras/$libName/${konanTarget.platformName}/include \
         |  -I/usr/local/kotlinxtras/libs/$libName/${konanTarget.platformName}/include
         |linkerOpts.${konanTarget.name} = -Lbuild/kotlinxtras/$libName/${konanTarget.platformName}/lib \
         |  -L/usr/local/kotlinxtras/libs/$libName/${konanTarget.platformName}/lib
         |libraryPaths.${konanTarget.name} = -Lbuild/kotlinxtras/$libName/${konanTarget.platformName}/lib \
         |  -L/usr/local/kotlinxtras/libs/$libName/${konanTarget.platformName}/lib    
         |""".trimMargin()
          )
        }
      }
    }
  }


}