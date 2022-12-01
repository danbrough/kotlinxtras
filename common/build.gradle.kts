import org.danbrough.kotlinxtras.declareSupportedTargets
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
  `maven-publish`
  //id("org.danbrough.kotlinxtras.sonatype")
//  listOf("openssl","iconv","sqlite").forEach {
//    id("org.danbrough.kotlinxtras.$it") version libs.versions.xtras.get()
//  }
}

//xtrasIconv {
//  buildEnabled = true
//}
//
//xtrasOpenssl {
//  buildEnabled = true
//}
//xtrasSqlite {
//  buildEnabled = true
//}


kotlin{

  declareSupportedTargets()

  val posixMain by sourceSets.creating

  val posix32Main by sourceSets.creating {
    dependsOn(posixMain)
  }

  val posix64Main by sourceSets.creating {
    dependsOn(posixMain)
  }

  targets.withType<KotlinNativeTarget>().all {
    compilations["main"].apply {
      if (konanTarget.architecture.bitness == 32)
        defaultSourceSet.dependsOn(posix32Main)
      else
        defaultSourceSet.dependsOn(posix64Main)
    }
  }
}
