plugins {
  //alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras)

  `java-gradle-plugin`
  alias(libs.plugins.kotlinXtras.wolfssl)


}


dependencies {
  //implementation(project(":wolfssl"))
}

