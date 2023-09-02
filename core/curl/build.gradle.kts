plugins {
  //alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras)
  `kotlin-dsl`
}

version = libs.versions.curl.get()

dependencies {
  implementation(project(":plugin"))
}

gradlePlugin {
  plugins {
    create("curl") {
      id = "$group.curl"
      implementationClass = "$group.curl.CurlPlugin"
    }
  }
}




