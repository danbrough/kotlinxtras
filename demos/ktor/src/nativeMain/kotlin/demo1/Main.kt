package demo1

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.curl.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import kotlinx.coroutines.runBlocking


private val log = klog.klog("demo1") {
  level = Level.TRACE
  writer = KLogWriters.stdOut
  messageFormatter = KMessageFormatters.verbose.colored
}

suspend fun runClient(engine: HttpClientEngine) {
  val client = HttpClient(engine)
  try {
    val response = client.get("http://example.com")
    log.debug(response.bodyAsText())
  } finally {
    // To prevent IllegalStateException https://youtrack.jetbrains.com/issue/KTOR-1071
    client.close()
    engine.close()
  }
}

fun main(args: Array<String>) {
  log.debug("running demo1")

  runBlocking {
    Platform.isMemoryLeakCheckerActive = false

    runClient(Curl.create())
  }
}