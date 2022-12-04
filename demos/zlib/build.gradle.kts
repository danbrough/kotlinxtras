import org.danbrough.kotlinxtras.binaries.LibraryExtension
import org.danbrough.kotlinxtras.binaries.git
import org.danbrough.kotlinxtras.binaries.registerLibraryExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {

  kotlin("multiplatform")
//  `kotlin-dsl`

  `maven-publish`
  id("org.danbrough.kotlinxtras.binaries")
  id("demo.zlib")
}

repositories {
  maven(file("/usr/local/kotlinxtras/build/m2"))
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}


xtrasZLib {
  cinterops {
    headerFile = project.file("src/zlib_cinterops_header.def")
    defFile = project.file("src/zlib_cinterops.def")
  }
}
//project.pluginManager.apply(ZLibPlugin::class.java)


/*project.registerLibraryExtension("zlib",LibraryExtension::class.java){

  git("1.2.13","04f42ceca40f73e2978b50e93806c2a18c1281fc")

  configure {target->
    commandLine("./configure","--prefix=${prefixDir(target)}")
  }

}*/


kotlin {
  linuxX64()

  val commonMain by sourceSets.getting{
    dependencies {
      implementation("org.danbrough:klog:0.0.1-beta07")
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget>{
    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }

    binaries {
      executable("zlibDemo") {
        entryPoint = "demo.zlib.main"
      }

    }
  }
}

