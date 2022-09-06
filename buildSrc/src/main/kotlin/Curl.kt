import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import BuildEnvironment.platformName

object Curl {
  const val TAG = "curl-7_85_0"
  
  fun KonanTarget.curlSrcDir(project: Project): File =
    project.buildDir.resolve("curl/$TAG/$platformName")


  fun KonanTarget.curlPrefix(project: Project): File =
    project.rootProject.file("libs/curl/$TAG/$platformName")
  
  
}