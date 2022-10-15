package org.danbrough.kotlinxtras.binaries

import org.danbrough.kotlinxtras.PropertiesPlugin
import org.danbrough.kotlinxtras.platformName
import org.danbrough.kotlinxtras.xtrasTaskGroup
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.dsl.kotlinExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.pm20.util.targets
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Files


open class BinariesProviderExtension(private val project: Project) {

  //Base name for the publications
  //Will default to the projects name
  var libName: String = project.name

  //KonanTargets for which to build binary archives for
  var supportedTargets = mutableListOf<KonanTarget>()

  //Where to store the archives.
  //Will default to $rootProject.buildDir/binaries/libName
  var archivesDir: File = project.rootProject.buildDir.resolve("binaries")


  var version: String = project.version.toString()

}

/*
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.gradle.api.tasks.Copy

class CopyWithSymlink extends Copy {
    public CopyWithSymlink() {
        super();
        eachFile { details ->
            Path sourcePath = FileSystems.getDefault().getPath(details.file.path)
            if(Files.isSymbolicLink(sourcePath)) {
                details.exclude()
                Path destinationPath = Paths.get("${destinationDir}/${details.relativePath}")
                if(Files.notExists(destinationPath.parent)) {
                    project.mkdir destinationPath.parent
                }
                project.exec {
                    commandLine 'ln', '-sf', Files.readSymbolicLink(sourcePath), destinationPath
                }
            }
        }
    }
}
 */



class BinariesProviderPlugin : Plugin<Project> {
  override fun apply(targetProject: Project) {

    targetProject.pluginManager.apply(PropertiesPlugin::class.java)

    val isMacHost = System.getProperty("os.name").startsWith("Mac")

    val extn =
      targetProject.extensions.create(
        "binariesProvider",
        BinariesProviderExtension::class.java,
        targetProject
      )

    targetProject.afterEvaluate {

      val libName = extn.libName

      val archivesDir = extn.archivesDir

      val supportedTargets =
        if (extn.supportedTargets.isEmpty())
          project.kotlinExtension.targets.filterIsInstance<KotlinNativeTarget>()
            .map { it.konanTarget } else extn.supportedTargets

      project.extensions.getByType<PublishingExtension>().apply {

        val repoNames = repositories.names


        val publishToReposTasks = repoNames.associateWith {
          project.tasks.create("publish${libName.capitalized()}BinariesTo${it.capitalized()}") {
            group = "publishing"
            version = extn.version
          }
        }

        //Support apple targets on mac host and everything else on what is assumed to be linux
        supportedTargets.filter { it.family.isAppleFamily == isMacHost }.forEach { target ->
          val jarName = "$libName${target.platformName.capitalized()}"

          val jarTask = project.tasks.register<Jar>("jar${jarName.capitalized()}Binaries") {
            archiveBaseName.set(jarName)
            dependsOn("build${target.platformName.capitalized()}")
            group = xtrasTaskGroup

            val srcDir = project.rootProject.file("libs/$libName/${target.platformName}")

//            doFirst {
//              Files.list(srcDir.toPath().resolve("lib")).filter { Files.isSymbolicLink(it) }
//                .forEach {
//                  println("FOUND LINK $it")
//                }
//            }

            from(srcDir) {
              //include("include/**","lib/*.so", "lib/*.so.*", "lib/*.a", "lib/*.dll", "lib/*.dylib")
              include("include/**", "lib/*")
            }

            into("$libName/${target.platformName}")
            destinationDirectory.set(archivesDir.resolve(extn.libName).resolve(extn.version))


            //to preserve symlinks
            eachFile {
              val sourcePath = FileSystems.getDefault().getPath(file.path)

              if (Files.isSymbolicLink(sourcePath)) {
                exclude()
             //   println("found symlink sourcePath:$sourcePath relatetivePath:$relativePath")

//                val destinationPath = Paths.get("${destinationDirectory.get()}/$relativeSourcePath")
//                println("sourcePath: $sourcePath -> $destinationPath  relativeSourcePath: ${this.relativeSourcePath}")
//                if (Files.notExists(destinationPath.parent)){
//                  mkdir(destinationPath.parent)
//                }
//                exec {
//                  commandLine("ln","-sf",Files.readSymbolicLink(sourcePath),destinationPath)
//                }
              }
            }
          }

          val publicationName = "$libName${target.platformName.capitalized()}"

          publications.register<MavenPublication>(publicationName) {
            artifactId = name
            groupId = "${project.group}.$libName.binaries"
            version = extn.version
            artifact(jarTask)
          }

          publishToReposTasks.forEach {
            it.value.dependsOn("publish${publicationName.capitalized()}PublicationTo${it.key.capitalized()}Repository")
          }

        }
      }
    }

  }
}