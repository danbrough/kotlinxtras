package org.danbrough.kotlinxtras

import org.gradle.api.Project
import java.io.File

const val XTRAS_PACKAGE = "org.danbrough.kotlinxtras"

const val XTRAS_BINARY_PLUGIN_ID = "$XTRAS_PACKAGE.binaries"

const val XTRAS_TASK_GROUP = "xtras"

const val PROPERTY_XTRAS_DIR = "xtras.dir"

/**
 * Location of the xtras packages directory
 */
const val PROPERTY_PACKAGES_DIR = "xtras.packages.dir"


/**
 * Location of the xtras downloads directory
 */
const val PROPERTY_DOWNLOADS_DIR = "xtras.downloads.dir"

const val PROPERTY_DOCS_DIR = "xtras.docs.dir"

const val PROPERTY_LIBS_DIR = "xtras.libs.dir"

const val PROPERTY_CINTEROPS_DIR = "xtras.cinterops.dir"

private fun Project.xtrasPath(name: String, defValue: String? = null): File =
  properties[name]?.toString()?.trim()?.let {
    project.file(it)
  } ?: defValue?.let { rootProject.xtrasDir.resolve(it) }
  ?: rootProject.buildDir.resolve("xtras")

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
 * Path to the xtras packages directory.
 * This is where binary archives are stored.
 *
 * Defaults to `project.xtrasDir.resolve("packages")`
 */
val Project.xtrasPackagesDir: File
  get() = xtrasPath(PROPERTY_PACKAGES_DIR, "packages")


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
 * This is the install prefix for binary packages
 *
 * Defaults to `project.xtrasDir.resolve("libs")`
 */
val Project.xtrasLibsDir: File
  get() = xtrasPath(PROPERTY_LIBS_DIR, "libs")


