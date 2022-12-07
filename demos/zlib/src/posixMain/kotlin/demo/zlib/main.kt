package demo.zlib

import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import klog.klog
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.memScoped


val log = klog("DEMO") {
  level = Level.TRACE
  writer = KLogWriters.stdOut
  messageFormatter = KMessageFormatters.verbose.colored
}


fun main() {
  log.info("running main ...")
  val level = Z_DEFAULT_COMPRESSION;
  /*
    if (argv[0][0] == '-') {
        if (argv[0][1] < '0' || argv[0][1] > '9' || argv[0][2] != 0)
            bye("invalid compression level", "");
        level = argv[0][1] - '0';
        if (*++argv == NULL) bye("no gzip file name after options", "");
    }
   */


  memScoped {
    
  }
}