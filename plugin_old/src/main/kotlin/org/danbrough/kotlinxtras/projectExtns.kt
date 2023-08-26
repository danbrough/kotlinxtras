package org.danbrough.kotlinxtras

import org.danbrough.kotlinxtras.binaries.binariesExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.SharedLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.targets
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File

fun Project.sharedLibraryPath(
  target: KonanTarget = HostManager.host,
  debuggable: Boolean = true
): String {

  val libPath =
    binariesExtension.libraryExtensions.map {
      it.libsDir(target).resolve("lib")
    }.toMutableList()

  libPath += kotlinExtension.targets.filterIsInstance<KotlinNativeTarget>()
    .firstOrNull { it.konanTarget == target }
    ?.binaries?.withType(SharedLibrary::class.java)
    ?.filter { it.debuggable == debuggable }
    ?.map {
      it.outputDirectory
    }
    ?: emptyList()

  return libPath.joinToString(File.pathSeparator)
}