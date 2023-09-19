package demo

actual fun demo(args: Array<String>){
  log.info("running the native demo")
  curlDemo(args)
}