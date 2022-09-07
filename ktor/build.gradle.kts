
val KTOR_TAG = "danbrough-2.1.0"

val ktorGitDir = file("src/ktor.git")
val ktorSrcDir = file("ktor")

val srcClone by tasks.registering(Exec::class) {
  commandLine(
    BuildEnvironment.gitBinary,
    "clone",
    "--bare",
    "https://github.com/danbrough/ktor",
    ktorGitDir
  )
  outputs.dir(ktorGitDir)
  onlyIf {
    !ktorGitDir.exists()
  }
}

val checkoutSrc by tasks.registering(Exec::class){
  dependsOn(srcClone)
  commandLine(
    BuildEnvironment.gitBinary,
    "checkout",
    "--bare",
    "https://github.com/danbrough/ktor",
    ktorGitDir
  )
}