package org.danbrough.kotlinxtras

import org.gradle.api.Project
import java.io.File


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

/**
 * Path to the top level xtras directory.
 * This is where sources and binary packages are downloaded and built.
 *
 * Defaults to `project.rootProject.buildDir.resolve("xtras")`
 */
val Project.xtrasDir: File
  get() =
    if (hasProperty(PROPERTY_XTRAS_DIR))
      file(property(PROPERTY_XTRAS_DIR)!!.toString().trim())
    else
      rootProject.buildDir.resolve("xtras")

/**
 * Path to the xtras downloads directory.
 * This is where source archives are downloaded to.
 *
 * Defaults to `project.xtrasDir.resolve("downloads")`
 */
val Project.xtrasDownloadsDir: File
  get() =
    if (hasProperty(PROPERTY_DOWNLOADS_DIR))
      file(property(PROPERTY_DOWNLOADS_DIR)!!.toString().trim())
    else
      xtrasDir.resolve("downloads")


/**
 * Path to the xtras packages directory.
 * This is where binary archives are stored.
 *
 * Defaults to `project.xtrasDir.resolve("packages")`
 */
val Project.xtrasPackagesDir: File
  get() =
    if (hasProperty(PROPERTY_PACKAGES_DIR))
      file(property(PROPERTY_PACKAGES_DIR)!!.toString().trim())
    else
      xtrasDir.resolve("packages")


/**
 * Path to the xtras kdocs directory.
 * This is where kdoc documentation is generated to.
 *
 * Defaults to `project.xtrasDir.resolve("docs")`
 */
val Project.xtrasDocsDir: File
  get() =
    if (hasProperty(PROPERTY_DOCS_DIR))
      file(property(PROPERTY_DOCS_DIR)!!.toString().trim())
    else
      xtrasDir.resolve("docs")



/**
 * Path to the xtras libs directory.
 * This is the install prefix for binary packages
 *
 * Defaults to `project.xtrasDir.resolve("libs")`
 */
val Project.xtrasLibsDir: File
  get() =
    if (hasProperty(PROPERTY_LIBS_DIR))
      file(property(PROPERTY_LIBS_DIR)!!.toString().trim())
    else
      xtrasDir.resolve("libs")



