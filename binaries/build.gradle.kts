import org.danbrough.kotlinxtras.IconvBinaryExtension


plugins {
  id("org.danbrough.kotlinxtras.sonatype")
  id("org.danbrough.kotlinxtras.iconv") version libs.versions.xtras.get()
}

xtrasBinaries {
  enableBuildSupportByDefault = true
  println("XtrasBinaries run: enableBuildSupportByDefault = $enableBuildSupportByDefault")
}

xtrasIconv {
  println("XtrasIconv run: buildEnabled: $buildEnabled")
  println("XtrasIconv run binaryConfiguration.enableBuildSupportByDefault =  ${binaries.enableBuildSupportByDefault}")
}


project.extensions.findByType(IconvBinaryExtension::class)?.also {
  println("Xtras: ICONV: buildEnabled: ${it.buildEnabled}")
  project.afterEvaluate {
    println("Xtras: ICONV2: buildEnabled: ${it.buildEnabled}")
  }
}