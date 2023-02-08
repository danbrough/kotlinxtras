package org.danbrough.kotlinxtras.binaries

import org.gradle.api.Project
import org.gradle.api.logging.LogLevel


internal fun Project.log(msg:String,level:LogLevel = LogLevel.INFO){
  logger.log(level, msg)

  val loggingProperty = "xtras.log.stdout"

  if (hasProperty(loggingProperty) && property(loggingProperty) == "true"){
    println("$level: $msg")
  }
}