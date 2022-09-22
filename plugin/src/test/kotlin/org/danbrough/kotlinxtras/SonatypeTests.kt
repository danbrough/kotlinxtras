package org.danbrough.kotlinxtras

import groovy.xml.XmlParser
import groovy.xml.XmlParserFactory
import klog.*
import org.gradle.kotlin.dsl.apply
import org.gradle.testfixtures.ProjectBuilder
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.io.ByteArrayInputStream
import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathFactory
import kotlin.test.Test

private val log by lazy {
  klog("org.danbrough.kotlinxtras") {
    level = Level.TRACE
    writer = KLogWriters.stdOut
    messageFormatter = KMessageFormatters.verbose.colored
  }
}

class SonatypeTests {
  //@Test
  fun sonaTest() {

    log.warn("running sona test")
    val project = ProjectBuilder.builder()
      .withName("testProject")
      .withProjectDir(File(".").canonicalFile.parentFile.also {
        log.warn("project dir: ${it.absolutePath}")
      })
      .build()


  }

  @Test
  fun parseResponse() {



  }
}