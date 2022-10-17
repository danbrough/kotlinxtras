package demo.curl

import demo.log
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import libcurl.CURLE_OK
import libcurl.CURLOPT_CAINFO
import libcurl.CURLOPT_FOLLOWLOCATION
import libcurl.CURLOPT_URL
import libcurl.curl_easy_cleanup
import libcurl.curl_easy_init
import libcurl.curl_easy_perform
import libcurl.curl_easy_setopt
import libcurl.curl_easy_strerror


fun main(args: Array<String>) {
  log.info("running main ..")

  val url = if (args.isEmpty()) "https://example.com" else args[0]

  log.debug("connecting to $url ..")

  memScoped {

    val curl = curl_easy_init()
    if (curl != null) {
      curl_easy_setopt(curl, CURLOPT_URL, url)
      curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L)

      //to specify a cacert.pem file (needed on at least android native and macos)
      platform.posix.getenv("CA_CERT_FILE")?.also {
        curl_easy_setopt(curl, CURLOPT_CAINFO, it)
      } ?: log.warn("CA_CERT_FILE should be set to cacert.pem.")


      val res = curl_easy_perform(curl)
      if (res != CURLE_OK) {
        log.error("curl_easy_perform() failed ${curl_easy_strerror(res)?.toKString()}\n")
      }
      curl_easy_cleanup(curl)
    }
  }

  log.info("done")

}