package demo

actual fun demo(args: Array<String>){
  println("RUNNING NATIVE DEMO")
  log.info("running the native demo")
  curlDemo(args)
}