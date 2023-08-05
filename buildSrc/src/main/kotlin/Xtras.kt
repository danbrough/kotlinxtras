import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec

object Xtras {
  const val projectGroup = "org.danbrough.kotlinxtras"
  const val version = "0.0.3-beta15"
  const val publishingVersion = "0.0.3-beta17"
  const val repoName = "xtras"
  const val javaLangVersion = 8
}

fun PluginDependenciesSpec.xtras(plugin: String): PluginDependencySpec =
  id("${Xtras.projectGroup}.$plugin")

fun PluginDependenciesSpec.xtras(plugin: String, version: String): PluginDependencySpec =
  id("${Xtras.projectGroup}.$plugin").version(version)
