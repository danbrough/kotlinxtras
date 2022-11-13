@file:Suppress("unused")

package org.danbrough.kotlinxtras

import org.gradle.api.Project



open class IconvExtension(project: Project, libName: String, buildEnvironment: BuildEnv) :
  BinaryExtension(project, libName, buildEnvironment)


class IconvPlugin : BinaryPlugin<IconvExtension>("iconv", IconvExtension::class.java) {
  override fun apply(target: Project) {
    super.apply(target)
  }
}




