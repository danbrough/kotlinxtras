package demo

import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import klog.klog
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.getenv
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import libcurl.*

val log = klog("DEMO") {
  messageFormatter = KMessageFormatters.verbose.colored
  level = Level.TRACE
  writer = KLogWriters.stdOut
}


@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
  log.info("running demo1")
  val url = if (args.isEmpty()) "https://example.com" else args[0]
  log.debug("connecting to $url ..")
  memScoped {

    val curl = curl_easy_init()
    if (curl != null) {
      curl_easy_setopt(curl, CURLOPT_URL, url)
      curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L)

      //to specify a cacert.pem file (needed on android native)
      getenv("CA_CERT_FILE")?.also {
        curl_easy_setopt(curl, CURLOPT_CAINFO, it)
      }
        ?: log.warn("Set environment CA_CERT_FILE to location of cacert.pem if there are ssl verification errors")


      val res = curl_easy_perform(curl)
      if (res != CURLE_OK) {
        log.error("curl_easy_perform() failed ${curl_easy_strerror(res)?.toKString()}\n")
      }
      curl_easy_cleanup(curl)
    }
  }
}
