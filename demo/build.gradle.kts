import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  alias(xtras.plugins.kotlin.multiplatform)
}

kotlin {
  linuxX64()

  //iosX64()
  //macosX64()

  val commonMain by sourceSets.getting {

  }

  val nativeMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget> {
    compilations["main"].apply {
      defaultSourceSet {
        dependsOn(nativeMain)
      }
    }

    binaries {
      executable("demo") {
        entryPoint("demo.main")
      }
    }
  }
}