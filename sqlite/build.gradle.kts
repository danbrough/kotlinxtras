import BuildEnvironment.buildEnvironment
import BuildEnvironment.declareNativeTargets
import BuildEnvironment.hostTriplet
import BuildEnvironment.konanDepsTask
import BuildEnvironment.platformName
import KotlinXtras_gradle.KotlinXtras.configureBinarySupport
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

plugins {
  kotlin("multiplatform")
  `maven-publish`
}


val sqliteGitDir = rootProject.file("repos/sqlite")
val sqliteVersion = project.properties["sqlite.version"]?.toString() ?: throw Error("Gradle property sqlite.version not set")

fun KonanTarget.sqliteSrcDir(project: Project): java.io.File =
  project.buildDir.resolve("sqlite/$platformName")


fun KonanTarget.sqlitePrefix(project: Project): java.io.File =
  project.rootProject.file("libs/sqlite/$platformName")

val KonanTarget.sqliteNotBuilt: Boolean
  get() = !sqlitePrefix(project).resolve("include/sqlite3.h").exists()

fun srcPrepare(target: KonanTarget): Exec =
  tasks.create("srcPrepare${target.platformName.capitalize()}", Exec::class) {
    val srcDir = target.sqliteSrcDir(project)
    outputs.dir(srcDir)
    commandLine(
      BuildEnvironment.gitBinary, "clone", sqliteGitDir, srcDir
    )
    onlyIf { target.sqliteNotBuilt && !srcDir.exists()}
  }




fun configureTask(target: KonanTarget) =
  tasks.register("srcConfigure${target.platformName.capitalize()}", Exec::class) {
    dependsOn("srcPrepare${target.platformName.capitalize()}")


    //to ensure the konan tools are available
    dependsOn(target.konanDepsTask(project))


    val srcDir = target.sqliteSrcDir(project)
    workingDir(srcDir)
    environment(target.buildEnvironment())

    val makefile = srcDir.resolve("Makefile")
    outputs.file(makefile)
    onlyIf {
      target.sqliteNotBuilt
    }

    val args = listOf(
      "./configure",
      "--host=${target.hostTriplet}",
      "--disable-tcl","--disable-readline",
      "--prefix=${target.sqlitePrefix(project)}",

      )
    commandLine(args)
  }


fun buildTask(target: KonanTarget) =
  tasks.register("build${target.platformName.capitalize()}", Exec::class) {
    dependsOn("srcConfigure${target.platformName.capitalize()}")
    val srcDir = target.sqliteSrcDir(project)
    val sqlitePrefixDir = target.sqlitePrefix(project)

    doFirst {
      println("building : $target")
    }
    outputs.file(sqlitePrefixDir.resolve("include/sqlite3.h"))

    onlyIf {
      target.sqliteNotBuilt
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
      srcPrepare(konanTarget)
      configureTask(konanTarget)
      buildTask(konanTarget).also {
        buildAll.dependsOn(it)
      }
    }

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
      cinterops.create("sqlite") {
        packageName("libsqlite")
        defFile("src/libsqlite.def")
      }
    }
  }


}


tasks.register("generateCInteropsDef") {
  inputs.file("src/libsqlite_header.def")
  outputs.file("src/libsqlite.def")
  doFirst {
    val outputFile = outputs.files.files.first()
    println("Generating $outputFile")
    outputFile.printWriter().use { output ->
      output.println(inputs.files.files.first().readText())
      kotlin.targets.withType<KotlinNativeTarget>().forEach {
        val konanTarget = it.konanTarget
        output.println("compilerOpts.${konanTarget.name} = -Ibuild/kotlinxtras/sqlite/${konanTarget.platformName}/include \\")
        output.println("\t-I/usr/local/kotlinxtras/libs/sqlite/${konanTarget.platformName}/include ")
        output.println("linkerOpts.${konanTarget.name} = -Lbuild/kotlinxtras/sqlite/${konanTarget.platformName}/lib \\")
        output.println("\t-L/usr/local/kotlinxtras/libs/sqlite/${konanTarget.platformName}/lib ")
        output.println("libraryPaths.${konanTarget.name} = build/kotlinxtras/sqlite/${konanTarget.platformName}/lib \\")
        output.println("\t/usr/local/kotlinxtras/libs/sqlite/${konanTarget.platformName}/lib ")
      }
    }
  }
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.CInteropProcess>() {
  dependsOn("generateCInteropsDef")
  if (BuildEnvironment.hostIsMac == konanTarget.family.isAppleFamily)
    dependsOn("build${konanTarget.platformName.capitalized()}")
}

project.configureBinarySupport(sqliteVersion)

