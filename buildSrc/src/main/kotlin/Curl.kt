import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import BuildEnvironment.platformName

object Curl {

  fun KonanTarget.curlSrcDir(project: Project): File =
    project.buildDir.resolve("curl/$platformName")


  fun KonanTarget.curlPrefix(project: Project): File =
    project.rootProject.file("libs/curl/$platformName")
  
  
}