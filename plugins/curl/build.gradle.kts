plugins {
  //alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.xtras)
  `kotlin-dsl`
}

version = libs.versions.curl.get()



gradlePlugin {
  plugins {
    create("curl") {
      id = "org.danbrough.kotlinxtras.curl"
      implementationClass = "$group.curl.CurlPlugin"
    }
  }
}




