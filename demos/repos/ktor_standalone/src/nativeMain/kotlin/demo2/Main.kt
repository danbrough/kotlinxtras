package demo2

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.curl.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*


fun main(args: Array<String>) {
  val log = klog.klog("demo2") {
    level = Level.TRACE
    writer = KLogWriters.stdOut
    messageFormatter = KMessageFormatters.verbose.colored
  }


  embeddedServer(CIO, port = 8080) {
    routing {
      get("/") {
        log.trace("processing request: ${call.request}")
        call.respondText("Hello world at ${Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())}\n")
      }
    }
  }.start(wait = true)
}