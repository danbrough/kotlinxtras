import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
  kotlin("multiplatform")
  id("com.squareup.sqldelight")
}

repositories{
  maven("../../build/m2")
  google()
  mavenCentral()
}

kotlin {

  linuxX64()
  //linuxArm64()

  val commonMain by sourceSets.getting
  val commonTest by sourceSets.getting{
    dependencies {
      implementation(kotlin("test"))
      implementation(libs.klog)
    }
  }

  val nativeMain by sourceSets.creating{
    dependencies {
      dependsOn(commonMain)
      implementation(libs.native.driver)
    }
  }

  val nativeTest by sourceSets.creating{
    dependencies {
      dependsOn(nativeMain)
    }
  }

  targets.withType(KotlinNativeTarget::class).all {
    compilations["main"].apply {
      defaultSourceSet.dependsOn(nativeMain)
    }
    compilations["test"].apply {
      defaultSourceSet.dependsOn(nativeTest)
    }
  }
}



sqldelight{
  database("Database"){
    packageName = "demo"
  }
}