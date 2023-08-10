package demo.hex

import demo.log
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString

@OptIn(ExperimentalForeignApi::class)
fun main() {
  log.debug("main()")
  memScoped {

    val msg = "Hello World!"
    val p = cValue<CPointerVar<ByteVar>>().ptr
    p[0] = msg.cstr.ptr

    println("m: ${p[0]?.toKString()}")
    libssh2.print_test(p)
    libssh2.ptr_test(p)
    libssh2.print_test(p)
    //libssh2.ptr_free(p)


  }
}