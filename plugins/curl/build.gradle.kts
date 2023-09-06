plugins {
  `kotlin-dsl`
  alias(libs.plugins.xtras)
}

version = libs.versions.curl.get()

gradlePlugin {
  plugins {
    create("curl") {
      id = "$group.curl"
      implementationClass = "$group.curl.CurlPlugin"
    }
  }
}




