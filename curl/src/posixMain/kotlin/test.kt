

import kotlinx.cinterop.cstr
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

fun downloadTest(url:String,caCertFile:String? = null) {
  memScoped {
    val curl = curl_easy_init()
    if (curl != null) {
      curl_easy_setopt(curl, CURLOPT_URL, url)
      curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L)
      caCertFile?.also {
        curl_easy_setopt(curl, CURLOPT_CAINFO, it.cstr)
      }
      val res = curl_easy_perform(curl)
      if (res != CURLE_OK) {
        println("curl_easy_perform() failed ${curl_easy_strerror(res)?.toKString()}\n")
      }
      curl_easy_cleanup(curl)
    }
  }
}