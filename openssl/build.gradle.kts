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

plugins {
  kotlin("multiplatform")
  `maven-publish`
}

val opensslGitDir = rootProject.file("repos/openssl")

val srcClone by tasks.registering(Exec::class) {
  commandLine(
    BuildEnvironment.gitBinary,
    "clone",
    OpenSSL.GIT_SRC,
    opensslGitDir
  )

  outputs.dir(opensslGitDir)
  onlyIf {
    !opensslGitDir.exists()
  }
}

fun srcPrepare(target: org.jetbrains.kotlin.konan.target.KonanTarget): Exec =
  tasks.create("srcPrepare${target.platformName.capitalize()}", Exec::class) {
    val srcDir = target.opensslSrcDir(project)
    dependsOn(srcClone)
    onlyIf {
      !srcDir.exists()
    }
    commandLine(
      BuildEnvironment.gitBinary, "clone", "--branch", OpenSSL.BRANCH, opensslGitDir, srcDir
    )
  }


fun configureTask(target: org.jetbrains.kotlin.konan.target.KonanTarget): Exec {

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
    if (target.family == org.jetbrains.kotlin.konan.target.Family.ANDROID) args += "-D__ANDROID_API__=${BuildEnvironment.androidNdkApiVersion} "
    else if (target.family == org.jetbrains.kotlin.konan.target.Family.MINGW) args += "--cross-compile-prefix=${target.hostTriplet}-"
    commandLine(args)
  }
}


fun buildTask(target: org.jetbrains.kotlin.konan.target.KonanTarget): TaskProvider<*> {
  val configureTask = configureTask(target)


  return tasks.register("build${target.platformName.capitalized()}", Exec::class) {
    target.opensslPrefix(project).resolve("lib/libssl.a").exists().also {
      isEnabled = !it
      configureTask.isEnabled = !it
    }
    dependsOn(configureTask.name)

    //to ensure the konan tools are available
    dependsOn(target.konanDepsTask(project))


    //tasks.getAt("buildAll").dependsOn(this)
    workingDir(target.opensslSrcDir(project))
    outputs.files(fileTree(target.opensslPrefix(project)) {
      include("lib/*.a", "lib/*.so", "lib/*.h", "lib/*.dylib")
    })
    environment(target.buildEnvironment())
    group = BasePlugin.BUILD_GROUP
    commandLine("make", "install_sw")
    doLast {
      println("STATUS: $status")
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


    buildTask(konanTarget).also {
      buildAll.dependsOn(it)
    }
/*
    compilations["main"].apply {
      cinterops.create("openssl") {
        packageName("libopenssl")
        defFile = project.file("src/openssl.def")
        includeDirs(konanTarget.opensslPrefix(project).resolve("include").also {
          println("USING include path: $it for target $konanTarget")
        })
        extraOpts(
          listOf(
            "-libraryPath",
            konanTarget.opensslPrefix(project).resolve("lib"),
            "-verbose"
          )
        )
      }
    }*/

    compilations["test"].apply {
      defaultSourceSet.dependsOn(nativeTest)
    }
  }
}