import org.danbrough.kotlinxtras.binaries.buildSources
import org.danbrough.kotlinxtras.binaries.configureSources
import org.danbrough.kotlinxtras.binaries.downloadSources
import org.danbrough.kotlinxtras.binaries.registerBinariesExtension
import org.danbrough.kotlinxtras.hostTriplet

plugins {
  `kotlin-dsl`
  `maven-publish`
  `signing`
  id("org.jetbrains.dokka")
  id("org.danbrough.kotlinxtras.xtras")
  id("org.danbrough.kotlinxtras.sonatype")
}

group = "org.danbrough.kotlinxtras"
version = "1.17c"

dependencies {
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("gradle-plugin-api"))
  implementation(libs.xtras)
}

gradlePlugin {
  plugins {
    create("iconvPlugin") {
      id = "$group.iconv"
      implementationClass = "IconvPlugin2"
      displayName = "KotlinXtras iconv plugin"
      description = "Provides iconv support to multi-platform projects"
    }
  }
}

class IconvPlugin2 : Plugin<Project> {
  override fun apply(target: Project) {
    println("APPLUING ICONV 2!!!")

    project.registerBinariesExtension("iconv").apply {
      version = "1.17c"

      downloadSources("https://ftp.gnu.org/pub/gnu/libiconv/libiconv-1.17.tar.gz") {
        stripTopDir = true
        tarExtractOptions = "xfz"
      }

      configureSources { target ->
        val sourcesDir = sourcesDir(target)
        commandLine(
          "./configure",
          "-C",
          "--enable-static",
          "--host=${target.hostTriplet}",
          "--prefix=${prefixDir(target)}"
        )
        outputs.file(sourcesDir.resolve("Makefile"))
      }

      buildSources { target ->
        commandLine("make", "install")
        outputs.dir(prefixDir(target))
      }


    }
  }

}