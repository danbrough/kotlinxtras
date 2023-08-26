package org.danbrough.kotlinxtras

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel

const val loggingProperty = "xtras.log.stdout"


fun Project.log(msg: String, level: LogLevel = LogLevel.INFO) {
  logger.log(level, msg)


  if (hasProperty(loggingProperty) && property(loggingProperty) == "true") {
    println("$level: $msg")
  }
}