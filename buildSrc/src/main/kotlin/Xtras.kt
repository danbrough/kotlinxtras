import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.PluginDependenciesSpecScope
import org.gradle.kotlin.dsl.version
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

object Xtras {
  const val projectGroup = "org.danbrough.kotlinxtras"
  const val version = "0.0.3-beta12"
  const val publishingVersion = "0.0.3-beta12"
}



fun PluginDependenciesSpec.xtras(plugin:String,group:String = Xtras.projectGroup): PluginDependencySpec =
  id("$group.$plugin")
