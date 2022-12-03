plugins {
  xtras("sonatype")
  xtras("iconv") version Xtras.version
}

xtrasBinaries {
  enableBuildSupportByDefault = true
  println("XtrasBinaries run: enableBuildSupportByDefault = $enableBuildSupportByDefault")
}

xtrasIconv {
  println("XtrasIconv run: buildEnabled: $buildEnabled")
  println("XtrasIconv run binaryConfiguration.enableBuildSupportByDefault =  ${binaries.enableBuildSupportByDefault}")
}




