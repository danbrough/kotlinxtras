import BuildEnvironment.buildEnvironment
import BuildEnvironment.konanDepsTask
import BuildEnvironment.hostTriplet
import BuildEnvironment.platformName
import OpenSSL.opensslPlatform
import OpenSSL.opensslPrefix
import OpenSSL.opensslSrcDir
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import BuildEnvironment.declareNativeTargets
import org.jetbrains.kotlin.konan.target.KonanTarget


plugins {
  kotlin("multiplatform")
 // `maven-publish`
}

val opensslGitDir = rootProject.file("repos/openssl")



fun srcPrepare(target: KonanTarget): Exec =
  tasks.create("srcPrepare${target.platformName.capitalize()}", Exec::class) {
    val srcDir = target.opensslSrcDir(project)
    onlyIf {
      !srcDir.exists()
    }
    commandLine(
      BuildEnvironment.gitBinary, "clone", opensslGitDir, srcDir
    )
  }


fun configureTask(target: KonanTarget): Exec {

  val srcPrepare = srcPrepare(target)

  return tasks.create("configure${target.platformName.capitalize()}", Exec::class) {
    dependsOn(srcPrepare)
    workingDir(target.opensslSrcDir(project))
    environment(target.buildEnvironment())
    doFirst {
      println("ENVIRONMENT: $environment")
    }
    val args = mutableListOf(
      "./Configure", target.opensslPlatform,
      "no-tests", "threads",
      "--prefix=${target.opensslPrefix(project)}",
      //"no-tests","no-ui-console", "--prefix=${target.opensslPrefix(project)}"
    )
    if (target.family == org.jetbrains.kotlin.konan.target.Family.ANDROID)
      args += "-D__ANDROID_API__=${BuildEnvironment.androidNdkApiVersion} "
    else if (target.family == org.jetbrains.kotlin.konan.target.Family.MINGW)
      args += "--cross-compile-prefix=${target.hostTriplet}-"
    commandLine(args)
  }
}


fun buildTask(target: KonanTarget): TaskProvider<*> {
  val configureTask = configureTask(target)


  return tasks.register("build${target.platformName.capitalized()}", Exec::class) {
    target.opensslPrefix(project).resolve("lib/libssl.a").exists().also {
      isEnabled = !it
      configureTask.isEnabled = !it
    }
    dependsOn(configureTask.name)

    //to ensure the konan tools are available
    dependsOn(target.konanDepsTask(project))

    workingDir(target.opensslSrcDir(project))
    outputs.files(fileTree(target.opensslPrefix(project)) {
      include("lib/*.a", "lib/*.so", "lib/*.h", "lib/*.dylib")
    })
    environment(target.buildEnvironment())
    group = BasePlugin.BUILD_GROUP
    commandLine("make", "install_sw")
    doLast {
      target.opensslSrcDir(project).deleteRecursively()
    }
  }
}
kotlin {
  declareNativeTargets()


  val nativeTest by sourceSets.creating {
    dependencies {
      //   implementation(Dependencies.klog)
    }
  }

  val buildAll = tasks.create("buildAll")

  targets.withType(KotlinNativeTarget::class).all {

    if (BuildEnvironment.hostIsMac == konanTarget.family.isAppleFamily) {
      buildTask(konanTarget).also {
        buildAll.dependsOn(it)
      }
    }

    compilations["test"].apply {
      defaultSourceSet.dependsOn(nativeTest)
    }
  }
}