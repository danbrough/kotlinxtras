package org.danbrough.xtras.tasks

import org.danbrough.xtras.library.XtrasLibrary
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Exec
import org.jetbrains.kotlin.konan.target.KonanTarget
import java.io.File
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintWriter
import java.io.Writer


fun  Exec.processStdout(
  output: Writer? = null,
  processLine: (String) -> Unit = { println(it) }
) {
  doFirst {
    val pin = PipedInputStream()
    this@processStdout.standardOutput = PipedOutputStream(pin)
    Thread {
      val printWriter = output?.let{PrintWriter(it)}
      InputStreamReader(pin).useLines {lines->
        lines.forEach { line ->
          printWriter?.also {
            it.println(line)
            it.flush()
          }
          processLine(line)
        }
      }
    }.start()
  }
}


