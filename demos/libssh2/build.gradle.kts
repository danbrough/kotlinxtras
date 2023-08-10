import org.danbrough.kotlinxtras.core.enableCurl
import org.danbrough.kotlinxtras.core.enableLibSSH2
import org.danbrough.kotlinxtras.core.enableOpenssl3

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras)
}


repositories {
  maven("/usr/local/kotlinxtras/build/xtras/maven")
  maven("https://s01.oss.sonatype.org/content/groups/staging")
  mavenCentral()
}

val openSSL = enableOpenssl3()

enableLibSSH2(openSSL) {

  cinterops {
    interopsPackage = "libssh2"

    headersSource = """
      void print_test(char** msg){
        printf("print_test(): <%s>\n",*msg);
      }
      void ptr_test(char** msg){
              *msg = "Message from C!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
              
              /**msg = malloc(256);
              memset(*msg,0,256);
              strncpy(*msg,"Hello World and stuff",44);*/
      }
      
      void ptr_free(char** msg){
        printf("doing a free\n");
        free(*msg);
      }
    """.trimIndent()
  }
}



kotlin {

  linuxX64()
  linuxArm64()

  if (HostManager.hostIsMac) {
    macosX64()
    macosArm64()
  }

  //androidNativeX86()

  val commonMain by sourceSets.getting {
    dependencies {
      implementation(libs.klog)
      implementation(libs.org.danbrough.kotlinxtras.common)
      implementation(libs.org.danbrough.kotlinxtras.utils)

      //implementation(libs.io.ktor.ktorutils)
    }
  }

  val posixMain by sourceSets.creating {
    dependsOn(commonMain)
  }

  targets.withType<KotlinNativeTarget> {

    compilations["main"].apply {
      defaultSourceSet.dependsOn(posixMain)
    }

    binaries {
      executable("ssh2Demo") {
        entryPoint = "demo.ssh2.main"
        findProperty("args")?.also {
          runTask?.args(it.toString().split(','))
        }
      }

      executable("hexDemo") {
        entryPoint = "demo.hex.main"
        findProperty("args")?.also {
          runTask?.args(it.toString())
        }
      }

    }
  }
}



tasks.create("runCurl") {
  dependsOn("runCurlDemoDebugExecutable${if (HostManager.hostIsMac) "MacosX64" else "LinuxX64"}")
}
