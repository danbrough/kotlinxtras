package org.danbrough.kotlinxtras

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.Locale

annotation class XtrasDSLMarker


class XtrasPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    project.log("applying XtrasPlugin to ${project.projectDir.absolutePath}")
  }
}


const val XTRAS_PACKAGE = "org.danbrough.kotlinxtras"

const val XTRAS_PLUGIN_ID = "$XTRAS_PACKAGE.xtras"

const val XTRAS_JAVA_LANG_VERSION = 8

const val XTRAS_BINARIES_PUBLISHING_GROUP = "$XTRAS_PACKAGE.xtras"

const val XTRAS_TASK_GROUP = "xtras"

const val XTRAS_REPO_NAME = "xtras"

const val PROPERTY_XTRAS_DIR = "xtras.dir"

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


const val PROPERTY_LIBS_DIR = "$PROPERTY_XTRAS_DIR.libs"

const val PROPERTY_CINTEROPS_DIR = "$PROPERTY_XTRAS_DIR.cinterops"

private fun Project.xtrasPath(name: String, defValue: String? = null): File =
  properties[name]?.toString()?.trim()?.let {
    project.file(it)
  } ?: defValue?.let { rootProject.xtrasDir.resolve(it) }
  ?: rootProject.layout.buildDirectory.dir("xtras").get().asFile

/**
 * Path to the top level xtras directory.
 * This is where sources and binary packages are downloaded and built.
 *
 * Defaults to `project.rootProject.buildDir.resolve("xtras")`
 */

val Project.xtrasDir: File
  get() = xtrasPath(PROPERTY_XTRAS_DIR)

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

fun String.capitalized() =
  replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun String.decapitalized() = replaceFirstChar { it.lowercase(Locale.getDefault()) }
