package org.danbrough.kotlinxtras.wolfssl

import org.danbrough.kotlinxtras.XtrasDSLMarker
import org.danbrough.kotlinxtras.hostTriplet
import org.danbrough.kotlinxtras.library.XtrasLibrary
import org.danbrough.kotlinxtras.library.xtrasCreateLibrary
import org.danbrough.kotlinxtras.library.xtrasRegisterSourceTask
import org.danbrough.kotlinxtras.log
import org.danbrough.kotlinxtras.source.gitSource
import org.gradle.api.Plugin
import org.gradle.api.Project

const val WOLFSSL_VERSION = "5.6.3"
const val WOLFSSL_COMMIT = "v5.6.3-stable"

object WolfSSL {
  const val extensionName = "wolfSSL"
  const val sourceURL = "https://github.com/wolfSSL/wolfssl.git"
}

class WolfSSLPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.log("WolfSSLPlugin.apply()")
  }
}


@XtrasDSLMarker
fun Project.xtrasWolfSSL(
  name: String = WolfSSL.extensionName,
  version: String = properties.getOrDefault("wolfssl.version",WOLFSSL_VERSION).toString(),
  commit: String = properties.getOrDefault("wolfssl.commit",  WOLFSSL_COMMIT).toString(),
  configure: XtrasLibrary.() -> Unit = {},
) = xtrasCreateLibrary(name, version) {
  gitSource(WolfSSL.sourceURL, commit)
  configure()

  supportedTargets.forEach { target ->
    val autogenTaskName = prepareSourceTaskName(target)
    xtrasRegisterSourceTask(autogenTaskName, target) {
      commandLine("./autogen.sh")
      outputs.file(workingDir.resolve("configure"))
    }

    val configureTaskName = configureTaskName(target)
    xtrasRegisterSourceTask(configureTaskName, target) {
      dependsOn(autogenTaskName)
      outputs.file(workingDir.resolve("Makefile"))
      val configureOptions = mutableListOf(
        "./configure",
        "--host=${target.hostTriplet}",
        "--prefix=${buildDir(target)}",
//      "--disable-fasthugemath",
//      "--disable-bump",
//      "--enable-opensslextra",
//      "--enable-fortress",
//      "--disable-debug",
//      "--disable-ntru",
//      "--disable-examples",
//      "--enable-distro",
//      "--enable-reproducible-build",
        "--enable-curve25519",
        "--enable-ed25519",
        "--enable-curve448",
        "--enable-ed448",
        "--enable-sha512",
        "--with-max-rsa-bits=8192",

        "--enable-certreq",//        Enable cert request generation (default: disabled)
        "--enable-certext",//        Enable cert request extensions (default: disabled)
//  --enable-certgencache   Enable decoded cert caching (default: disabled)
        //--enable-altcertchains  Enable using alternative certificate chains, only
        //   require leaf certificate to validate to trust root
        //--enable-testcert       Enable Test Cert (default: disabled)
        "--enable-certservice",
        "--enable-altcertchains",
//      "--enable-writedup",

        "--enable-opensslextra",
        //"--enable-openssh",
        //"--enable-libssh2",
        "--enable-keygen",
        "--enable-certgen",
        "--enable-ssh",
        "--enable-wolfssh",
        //"--disable-examples",
        "--enable-postauth",

        )

      commandLine(configureOptions)

    }

    val buildTaskName = buildTaskName(target)
    xtrasRegisterSourceTask(buildTaskName, target) {
      dependsOn(configureTaskName)
      outputs.dir(buildDir(target))
      commandLine("make", "install")
    }
  }
}
