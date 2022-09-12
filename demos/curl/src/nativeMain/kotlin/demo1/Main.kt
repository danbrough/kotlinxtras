package demo1
import klog.*
import kotlinx.cinterop.cstr
import kotlinx.cinterop.toKString
import libcurl.*

val log = klog.klog("demo1"){
  writer = KLogWriters.stdOut
  messageFormatter = KMessageFormatters.verbose.colored
  level = Level.TRACE
}


fun main(args:Array<String>){
  log.info("running main ..")

  val url  = if (args.isEmpty()) "https://example.com" else args[0]

  log.debug("connecting to $url ..")


  val curl = curl_easy_init()
  if (curl != null) {
    curl_easy_setopt(curl, CURLOPT_URL, url)
    curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L)
    curl_easy_setopt(curl, CURLOPT_CAINFO,"/data/cacert.pem".cstr)

    val res = curl_easy_perform(curl)
    if (res != CURLE_OK) {
      log.error("curl_easy_perform() failed ${curl_easy_strerror(res)?.toKString()}\n")
    }
    curl_easy_cleanup(curl)
  }

}