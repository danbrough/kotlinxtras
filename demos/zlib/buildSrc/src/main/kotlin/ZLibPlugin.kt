import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import javax.inject.Inject
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.danbrough.kotlinxtras.binaries.LibraryExtension
import org.danbrough.kotlinxtras.binaries.git
import org.danbrough.kotlinxtras.binaries.registerLibraryExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

open class ZLibLibraryExtension(project: Project) : LibraryExtension(project, "zlib")

class ZLibPlugin : Plugin<Project> {

  @Inject
  constructor()

  override fun apply(target: Project) {
    target.registerLibraryExtension("xtrasZLib", ZLibLibraryExtension::class.java) {

      version = "1.2.13"
      git("https://github.com/madler/zlib.git", "04f42ceca40f73e2978b50e93806c2a18c1281fc")

      supportedTargets = listOf(
        KonanTarget.LINUX_X64,
        KonanTarget.LINUX_ARM64,
        KonanTarget.LINUX_ARM32_HFP,
        KonanTarget.ANDROID_X86
      )

      buildEnabled = true

      cinterops {


        headers = """
          headers = zlib.h zconf.h
        """.trimIndent()
        /*
                defFile = file("src/zlib_cinterops.def")
        headersFile = file("src/zlib_cinterops_headers.def")
         */
      }

      configure { target ->
        commandLine("./configure", "--prefix=${prefixDir(target)}")
      }

      build {
        commandLine(binaries.makeBinary, "install")
      }
    }
  }

}