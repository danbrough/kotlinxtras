package demo

import kotlin.jvm.JvmStatic

expect fun demo(args: Array<String>)


class Demo {

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      log.debug("running the demo")
      demo(args)
    }
  }
}

fun main(args: Array<String>){
  Demo.main(args)
}