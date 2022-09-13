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
import org.jetbrains.kotlin.gradle.tasks.CInteropProcess
import org.jetbrains.kotlin.konan.target.KonanTarget


plugins {
  kotlin("multiplatform")
  `maven-publish`
}

val opensslGitDir = rootProject.file("repos/openssl")


val generateCInteropsDef by tasks.creating {
  inputs.file("src/openssl_header.def")
  outputs.file("src/openssl.def")
  doFirst {
    val outputFile = outputs.files.files.first()
    println("Generating $outputFile")

    outputFile.printWriter().use { output->
      output.println(inputs.files.files.first().readText())
      kotlin.targets.withType<KotlinNativeTarget>().forEach {
        val konanTarget = it.konanTarget
        output.println("compilerOpts.${konanTarget.name} = -Ibuild/libs/openssl/${konanTarget.platformName}/include \\")
        output.println("\t-I/usr/local/kotlinxtras/libs/openssl/${konanTarget.platformName}/include ")
        output.println("linkerOpts.${konanTarget.name} = -Lbuild/libs/openssl/${konanTarget.platformName}/lib \\")
        output.println("\t-L/usr/local/kotlinxtras/libs/openssl/${konanTarget.platformName}/lib ")
      }
    }
  }
}

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
    val installDir = target.opensslPrefix(project)

   if (!installDir.exists())
      dependsOn(configureTask)


    onlyIf{
      !installDir.exists()
    }
    //to ensure the konan tools are available
    dependsOn(target.konanDepsTask(project))

    workingDir(target.opensslSrcDir(project))


    outputs.files(fileTree(installDir) {
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


  val nativeTest by sourceSets.creating
  val nativeMain by sourceSets.creating

  val buildAll = tasks.create("buildAll")

  targets.withType(KotlinNativeTarget::class).all {

    if (BuildEnvironment.hostIsMac == konanTarget.family.isAppleFamily) {
      buildTask(konanTarget).also {
        buildAll.dependsOn(it)
      }
    }

    compilations["main"].apply {
      cinterops.create("libopenssl"){
        packageName("libopenssl")
        defFile("src/openssl.def")

      }

      defaultSourceSet.dependsOn(nativeMain)
    }

    compilations["test"].apply {
      defaultSourceSet.dependsOn(nativeTest)
    }
  }
}

tasks.withType<CInteropProcess>(){
  dependsOn(generateCInteropsDef)
}