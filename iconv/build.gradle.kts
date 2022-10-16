
import BuildEnvironment.buildEnvironment
import BuildEnvironment.declareNativeTargets
import BuildEnvironment.hostTriplet
import BuildEnvironment.konanDepsTaskName
import BuildEnvironment.platformName
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.de.undercouch.gradle.tasks.download.Download
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform")
  `maven-publish`
  id("org.danbrough.kotlinxtras.provider")
}

group = "org.danbrough.kotlinxtras.iconv"

val iconvVersion = project.properties["iconv.version"]?.toString() ?: throw Error("iconv.version not set")

binariesProvider {
  version = iconvVersion
}

fun KonanTarget.iconvSrcDir(project: Project): java.io.File =
  project.buildDir.resolve("iconv/$platformName/${project.properties["iconv.version"]}")


fun KonanTarget.iconvPrefix(project: Project): java.io.File =
  project.rootProject.file("libs/iconv/$platformName")

val KonanTarget.iconvNotBuilt: Boolean
  get() = !iconvPrefix(project).resolve("lib/libiconv.la").exists()


val downloadSrcTask by tasks.creating(Download::class.java) {
  src(project.properties["iconv.source"])
  dest(buildDir.resolve("iconv").also {
    if (!it.exists()) it.mkdirs()
  })
  overwrite(false)
}

fun srcPrepareFromDownload(target: KonanTarget): Copy =
  tasks.create<Copy>("srcPrepareFromDownload${target.platformName.capitalize()}") {
    dependsOn(downloadSrcTask)
    val srcDir = target.iconvSrcDir(project)
    from(tarTree(resources.gzip(downloadSrcTask.outputFiles.first()))) {
      eachFile {
        path = path.substring(relativePath.segments[0].length)
      }
    }
    into(srcDir)
    onlyIf { target.iconvNotBuilt }
  }


fun configureTask(target: KonanTarget) =
  tasks.register("srcConfigure${target.platformName.capitalize()}", Exec::class) {
    //dependsOn("srcPrepare${target.platformName.capitalize()}")
    dependsOn("srcPrepareFromDownload${target.platformName.capitalize()}")


    //to ensure the konan tools are available
    dependsOn(target.konanDepsTaskName)


    val srcDir = target.iconvSrcDir(project)
    workingDir(srcDir)
    environment(target.buildEnvironment())

    val makefile = srcDir.resolve("Makefile")
    outputs.file(makefile)
    onlyIf {
      target.iconvNotBuilt
    }

    val args = listOf(
      "./configure", "-C",
      "--host=${target.hostTriplet}",
      "--prefix=${target.iconvPrefix(project)}",
      )
    commandLine(args)
  }


fun buildTask(target: KonanTarget) =
  tasks.register("build${target.platformName.capitalize()}", Exec::class) {
    dependsOn("srcConfigure${target.platformName.capitalize()}")
    val srcDir = target.iconvSrcDir(project)
    val prefixDir = target.iconvPrefix(project)

    doFirst {
      println("building : $target")
    }
    outputs.file(prefixDir.resolve("lib/libiconv.la"))

    onlyIf {
      target.iconvNotBuilt
    }
    workingDir(srcDir)
    environment(target.buildEnvironment())

    commandLine("make", "install")
    doLast {
      if (didWork)
        srcDir.deleteRecursively()
    }
  }


kotlin {

  declareNativeTargets()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  val buildAll by tasks.creating


  targets.withType(KotlinNativeTarget::class).all {

    if (BuildEnvironment.hostIsMac == konanTarget.family.isAppleFamily) {
      //srcPrepare(konanTarget)
      srcPrepareFromDownload(konanTarget)
      configureTask(konanTarget)
      buildTask(konanTarget).also {
        buildAll.dependsOn(it)
      }
    }

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
      cinterops.create("libiconv") {
        packageName("libiconv")
        defFile("src/libiconv.def")
      }
    }
  }


}


tasks.register("generateCInteropsDef") {
  inputs.file("src/libiconv_header.def")
  outputs.file("src/libiconv.def")
  doFirst {
    val outputFile = outputs.files.files.first()
    println("Generating $outputFile")
    outputFile.printWriter().use { output ->
      output.println(inputs.files.files.first().readText())
      kotlin.targets.withType<KotlinNativeTarget>().forEach {
        val konanTarget = it.konanTarget
        output.println("compilerOpts.${konanTarget.name} = -Ibuild/kotlinxtras/iconv/${konanTarget.platformName}/include \\")
        output.println("\t-I/usr/local/kotlinxtras/libs/iconv/${konanTarget.platformName}/include ")
        output.println("linkerOpts.${konanTarget.name} = -Lbuild/kotlinxtras/iconv/${konanTarget.platformName}/lib \\")
        output.println("\t-L/usr/local/kotlinxtras/libs/iconv/${konanTarget.platformName}/lib ")
        output.println("libraryPaths.${konanTarget.name} = build/kotlinxtras/iconv/${konanTarget.platformName}/lib \\")
        output.println("\t/usr/local/kotlinxtras/libs/iconv/${konanTarget.platformName}/lib ")
      }
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.CInteropProcess>() {
  dependsOn("generateCInteropsDef")
  if (BuildEnvironment.hostIsMac == konanTarget.family.isAppleFamily)
    dependsOn("build${konanTarget.platformName.capitalized()}")
}


