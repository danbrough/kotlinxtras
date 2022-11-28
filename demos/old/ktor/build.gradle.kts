import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

plugins {
  kotlin("multiplatform")
}


repositories {
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  mavenCentral()
}


kotlin {


  linuxX64()
  linuxArm64()
  linuxArm32Hfp()


  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(libs.ktor.client.core)
      implementation(libs.ktor.client.curl)
      implementation(libs.kotlinx.coroutines.core)
    }
  }


  val nativeMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget>().all {


    compilations["main"].apply {
      defaultSourceSet.dependsOn(nativeMain)
    }

    binaries {

      executable("demo1") {
        entryPoint = "demo1.main"
        linkTask.doFirst {
          val curlDir = File("/usr/local/kotlinxtras/lib/curl/")
          if (!curlDir.exists())
            project.logger.warn("""$curlDir doesn't exist. Have you built curl?. 
              |Run ./gradlew curl:buildLinuxX64 in /usr/local/kotlinxtras or try the standalone demo""".trimMargin())
        }
      }
    }
  }
}







