package demo1

import klog.KLogWriters
import klog.KMessageFormatters
import klog.Level
import klog.colored
import kotlinx.cinterop.*
import libiconv.iconv
import libiconv.iconv_close
import libiconv.iconv_open
import org.danbrough.kotlinxtras.toSizeT
import platform.posix.*


@OptIn(UnsafeNumber::class)
val s:size_t = 0u

val log =   klog.klog("demo1"){
  level = Level.TRACE
  messageFormatter = KMessageFormatters.verbose.colored
  writer = KLogWriters.stdOut
}

fun main(){

  memScoped {


    log.info("calling iconv_open ..")

    val conv = iconv_open!!("UTF-8".cstr.ptr,"GBK".cstr.ptr)

    if (conv.toLong() == -1L){
      val err = posix_errno()
      log.error("failed: $err : ${strerror(err)?.toKString()}")
      return@memScoped
    }

    val input = intArrayOf(0xB5,0xE7,0xCA,0xD3,0xBB,0xFA).map{it.toByte()}.toByteArray()

    val output =ByteArray(128){
      0.toByte()
    }

    val inputSize =   alloc<size_tVar>().also {
      it.value = input.size.toSizeT()
    }



    val outputSize = alloc<size_tVar>().also {
      it.value = output.size.toSizeT()
    }

    input.usePinned {inputPinned->
      output.usePinned {outputPinned->

        val inbuf = alloc<CPointerVar<ByteVar>>().also {
          it.value = inputPinned.addressOf(0)
        }

        val outbuf = alloc<CPointerVar<ByteVar>>().also {
          it.value = outputPinned.addressOf(0)
        }

        if (iconv!!(conv,inbuf.ptr,inputSize.ptr,outbuf.ptr,outputSize.ptr).toLong() == -1L){
          val err = posix_errno()
          log.error("iconv failed: $err : ${strerror(err)?.toKString()}")
          return@memScoped
        }

        log.trace("output remaining: ${outputSize.value}")
      }


      val outputString = output.decodeToString(0,output.size - outputSize.value.toInt())
      val expected = "电视机"

      if (outputString != expected){
        log.error("Invalid output: $outputString  Expecting: $expected")
        log.debug("${outputString.encodeToByteArray().joinToString(",")} != ${expected.encodeToByteArray().joinToString(",")}")
      }

      log.debug("output: ${output.decodeToString()}")

      if (iconv_close!!(conv).toLong() == -1L){
        val err = posix_errno()
        log.error("close failed: $err : ${strerror(err)?.toKString()}")
      }
    }
  }
}