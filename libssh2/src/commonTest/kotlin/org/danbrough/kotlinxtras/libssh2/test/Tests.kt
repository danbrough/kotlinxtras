package org.danbrough.kotlinxtras.libssh2.test

import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import org.danbrough.kotlinxtras.libssh2.LibSSH2
import kotlin.test.Test


class Tests {
  companion object {

    val log = klog.klog("LIBSSH2") {
      writer = KLogWriters.stdOut
      messageFormatter = KMessageFormatters.verbose.colored
      level = Level.TRACE
    }

  }

  @Test
  fun test1() {
    log.info("test1()")
    val thang = LibSSH2()
    log.debug("libssh2 message: ${thang.message}")
  }
}