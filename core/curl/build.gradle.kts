@file:Suppress("PropertyName")


plugins {
  //alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras)
  `kotlin-dsl`
}

//group = "org.danbrough.kotlinxtras"
version = "0.0.4-beta01"

dependencies {
  //implementation("org.danbrough.kotlinxtras:plugin:0.0.3-beta19")
  implementation(project(":plugin"))
  //implementation(kotlin("gradle-plugin"))
}


gradlePlugin {
  plugins {
    create("curl") {
      id = "$group.curl"
      implementationClass = "$group.curl.CurlPlugin"
    }
  }
}




