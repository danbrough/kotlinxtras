import BuildEnvironment.platformName
import Curl.curlPrefix
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

plugins {
  kotlin("multiplatform") apply false
  `maven-publish`
}

ProjectProperties.init(project)

group = ProjectProperties.projectGroup
version = ProjectProperties.buildVersionName


allprojects {
  repositories {
    maven(Dependencies.SONA_STAGING)
    mavenCentral()
  }
}



enum class LibJarType {
  INCLUDES,SHARED,STATIC
}


val binariesGroup = "binaries"

fun createLibraryJar(target: KonanTarget, libName: String,jarType: LibJarType) : Jar{
  val jarName = "$libName${target.platformName.capitalized()}${jarType.name.toLowerCaseAsciiOnly().capitalized()}"

  return tasks.create<Jar>("${jarName}Jar") {
    archiveBaseName.set(jarName)
    group = binariesGroup
    from(project.file("libs").resolve(libName).resolve(target.platformName).resolve (
      when(jarType){
        Build_gradle.LibJarType.INCLUDES -> "include"
        else -> "lib"
      }
    ))

    when(jarType){
      Build_gradle.LibJarType.INCLUDES -> include("**")
      Build_gradle.LibJarType.SHARED -> include("*.so", "*.dll", "*.dylib")
      Build_gradle.LibJarType.STATIC -> include("*.a")
    }

    destinationDirectory.set(project.buildDir.resolve("jars"))
  }
}


fun createLibraryJars(target: KonanTarget, libName: String) {
  val jarsTask = tasks.create("${libName}${target.platformName.capitalized()}Jars"){
    group = binariesGroup
  }
  LibJarType.values().forEach { jarType->
    createLibraryJar(target,libName,jarType).also {
      jarsTask.dependsOn(it)
    }
  }
}

/*
setOf("curl","openssl").forEach { libName ->
  setOf(KonanTarget.LINUX_ARM32_HFP,KonanTarget.LINUX_ARM64,KonanTarget.LINUX_X64).forEach {target->
    createLibraryJars(target, libName)
  }
}
*/


publishing {
  repositories {
    maven(project.buildDir.resolve("m2")) {
      name = "m2"
    }
  }
  publications {
    create<MavenPublication>("curlBinaries") {
      //artifactId = "curlBinaries"
      artifact(createLibraryJar(KonanTarget.LINUX_X64,"curl",Build_gradle.LibJarType.SHARED))
    }
  }
}

