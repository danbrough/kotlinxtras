@file:Suppress("unused")

package org.danbrough.xtras

import org.danbrough.xtras.env.BuildEnvironment
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.extraProperties
import java.io.File
import java.util.Locale

annotation class XtrasDSLMarker




const val XTRAS_PACKAGE = "org.danbrough.xtras"

const val XTRAS_PLUGIN_ID = "$XTRAS_PACKAGE.xtras"

const val XTRAS_JAVA_LANG_VERSION = 8

const val XTRAS_BINARIES_PUBLISHING_GROUP = "$XTRAS_PACKAGE.xtras"

const val XTRAS_TASK_GROUP = "xtras"

const val XTRAS_REPO_NAME = "xtras"

const val PROPERTY_XTRAS_DIR = "xtras.dir"

const val PROPERTY_NDK_DIR = "xtras.dir.ndk"

/**
 * Location of the xtras packages directory
 */
const val PROPERTY_PACKAGES_DIR = "$PROPERTY_XTRAS_DIR.packages"


/**
 * Location of the xtras packages directory
 */
const val PROPERTY_MAVEN_DIR = "$PROPERTY_XTRAS_DIR.maven"

/**
 * Location of the xtras downloads directory
 */
const val PROPERTY_DOWNLOADS_DIR = "$PROPERTY_XTRAS_DIR.downloads"

const val PROPERTY_DOCS_DIR = "$PROPERTY_XTRAS_DIR.docs"

const val PROPERTY_BUILD_DIR = "$PROPERTY_XTRAS_DIR.build"

const val PROPERTY_SOURCE_DIR = "$PROPERTY_XTRAS_DIR.src"

const val PROPERTY_LOGS_DIR = "$PROPERTY_XTRAS_DIR.logs"

const val PROPERTY_LIBS_DIR = "$PROPERTY_XTRAS_DIR.libs"

const val PROPERTY_CINTEROPS_DIR = "$PROPERTY_XTRAS_DIR.cinterops"

private fun Project.xtrasPath(name: String, defValue: String? = null,default:(String)->File = {error("property $it not found")}): File =
  properties[name]?.toString()?.trim()?.let {
    if (it.startsWith("./")){
      project.rootDir.resolve(it.substringAfter("./"))
    }
    else project.file(it)
  } ?: defValue?.let { rootProject.xtrasDir.resolve(it) }
  ?: default(name)
 // ?: rootProject.layout.buildDirectory.dir("xtras").get().asFile

/**
 * Path to the top level xtras directory.
 * This is where sources and binary packages are downloaded and built.
 *
 * Defaults to `project.rootProject.buildDir.resolve("xtras")`
 */

val Project.xtrasDir: File
  get() = xtrasPath(PROPERTY_XTRAS_DIR){
    layout.buildDirectory.asFile.get().resolve("xtras")
  }

/**
 * Path to the xtras downloads directory.
 * This is where source archives are downloaded to.
 *
 * Defaults to `project.xtrasDir.resolve("downloads")`
 */
val Project.xtrasDownloadsDir: File
  get() = xtrasPath(PROPERTY_DOWNLOADS_DIR, "downloads")


/**
 * Path to the xtras build directory.
 * This is the prefix directory for compiled source code.
 *
 * Defaults to `project.xtrasDir.resolve("build")`
 */
val Project.xtrasBuildDir: File
  get() = xtrasPath(PROPERTY_BUILD_DIR, "build")


/**
 * Path to the xtras source directory.
 * This is where source code is located
 *
 * Defaults to `project.xtrasDir.resolve("src")`
 */
val Project.xtrasSourceDir: File
  get() = xtrasPath(PROPERTY_SOURCE_DIR, "src")

/**
 * Path to the xtras packages directory.
 * This is where binary archives are stored.
 *
 * Defaults to `project.xtrasDir.resolve("packages")`
 */
val Project.xtrasPackagesDir: File
  get() = xtrasPath(PROPERTY_PACKAGES_DIR, "packages")


/**
 * Path to the xtras logs directory.
 * This is where log output of build tasks are stored.
 *
 * Defaults to `project.xtrasDir.resolve("logs")`
 */
val Project.xtrasLogsDir: File
  get() = xtrasPath(PROPERTY_LOGS_DIR, "logs")

/**
 * Path to the xtras maven directory.
 * This is where binary archives are published to.
 *
 * Defaults to `project.xtrasDir.resolve("maven")`
 */
val Project.xtrasMavenDir: File
  get() = xtrasPath(PROPERTY_MAVEN_DIR, "maven")


/**
 * Path to the xtras cinterops directory.
 * This is where cinterop files are generated from headers.
 *
 * Defaults to `project.xtrasDir.resolve("cinterops")`
 */
val Project.xtrasCInteropsDir: File
  get() = xtrasPath(PROPERTY_CINTEROPS_DIR, "cinterops")


/**
 * Path to the xtras kdocs directory.
 * This is where kdoc documentation is generated to.
 *
 * Defaults to `project.xtrasDir.resolve("docs")`
 */
val Project.xtrasDocsDir: File
  get() = xtrasPath(PROPERTY_DOCS_DIR, "docs")


/**
 * Path to the xtras libs directory.
 * This where binary packages are extracted to.
 *
 * Defaults to `project.xtrasDir.resolve("libs")`
 */
val Project.xtrasLibsDir: File
  get() = xtrasPath(PROPERTY_LIBS_DIR, "libs")


/**
 * Path to the ndk directory.
 * Defaults to the environment variable ANDROID_NDK_ROOT then ANDROID_NDK_HOME
 */
val Project.xtrasNdkDir: File
  get() = xtrasPath(PROPERTY_NDK_DIR)


fun String.capitalized() =
  replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun String.decapitalized() = replaceFirstChar { it.lowercase(Locale.getDefault()) }
