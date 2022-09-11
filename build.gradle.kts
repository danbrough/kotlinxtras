import BuildEnvironment.platformName
import Curl.curlPrefix
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

plugins {
  kotlin("multiplatform") apply false
  //id("io.github.gradle-nexus.publish-plugin")
  `maven-publish`
  signing
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
  INCLUDES, SHARED, STATIC
}


val binariesGroup = "binaries"

fun createLibraryJar(target: KonanTarget, libName: String): Jar {
  val jarName = "$libName${target.platformName.capitalized()}Binaries"

  return tasks.create<Jar>("${jarName}Jar") {
    archiveBaseName.set(jarName)
    group = binariesGroup
    from(project.file("libs").resolve(libName).resolve(target.platformName))

    include("include/**")
    include("lib/*.so")
    include("lib/*.dll")
    include("lib/*.dylib")
    include("lib/*.a")
    /*when(jarType){
      Build_gradle.LibJarType.INCLUDES -> include("**")
      Build_gradle.LibJarType.SHARED -> include("*.so", "*.dll", "*.dylib")
      Build_gradle.LibJarType.STATIC -> include("*.a")
    }*/

    destinationDirectory.set(project.buildDir.resolve("jars"))
  }
}


/*fun createLibraryJars(target: KonanTarget, libName: String) {
  val jarsTask = tasks.create("${libName}${target.platformName.capitalized()}Jars"){
    group = binariesGroup
  }
  LibJarType.values().forEach { jarType->
    createLibraryJar(target,libName,jarType).also {
      jarsTask.dependsOn(it)
    }
  }
}*/

/*
setOf("curl","openssl").forEach { libName ->
  setOf(KonanTarget.LINUX_ARM32_HFP,KonanTarget.LINUX_ARM64,KonanTarget.LINUX_X64).forEach {target->
    createLibraryJars(target, libName)
  }
}
*/
/*

nexusPublishing {
  repositories {
    sonatype {
      nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
      snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
    }
  }
}
*/


publishing {
  repositories {
    maven(project.buildDir.resolve("m2")) {
      name = "m2"
    }
    maven(Dependencies.SONA_STAGING)
  }
  publications {
    setOf("curl", "openssl").forEach { libName ->
      setOf(
        KonanTarget.LINUX_ARM32_HFP,
        KonanTarget.LINUX_ARM64,
        KonanTarget.LINUX_X64
      ).forEach { target ->
        create<MavenPublication>("$libName${target.platformName.capitalized()}Binaries") {
          artifactId = name
          artifact(createLibraryJar(target, libName))
        }
      }
    }
  }


}

if (project.hasProperty("signPublications"))
  signing {
    sign(publishing.publications)
  }

