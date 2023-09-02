plugins {
  //alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.kotlinXtras)
  `kotlin-dsl`
}

version = libs.versions.wolfssh.get()

dependencies {
  implementation(project(":plugin"))
}

gradlePlugin {
  plugins {
    create("wolfssh") {
      id = "$group.wolfssh"
      implementationClass = "$group.wolfssh.WolfSSHPlugin"
    }
  }
}

