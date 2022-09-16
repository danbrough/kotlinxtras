
plugins {
  kotlin("jvm")
  `java-gradle-plugin`
  `maven-publish`
}

group = "org.danbrough.kotlinxtras"

java {
  dependencies {
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
    implementation(kotlin("gradle-plugin"))
  }
}


gradlePlugin {
  plugins {
    create("xtrasPlugin") {
      id = "org.danbrough.kotlinxtras.xtras"
      implementationClass = "org.danbrough.kotlinxtras.XtrasPlugin"
    }
  }
}

publishing {
  repositories {
    maven("../build/m2"){
      name = "m2"
    }
  }
}