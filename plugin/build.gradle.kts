
plugins {
  /*`java-gradle-plugin`
  `maven-publish`*/
  //id("com.gradle.plugin-publish") version "1.0.0"
  kotlin("jvm")
  `java-gradle-plugin`
  `maven-publish`
}
group = "org.danbrough.kotlinxtras"
version = "0.0.1-alpha05"

java {
  dependencies {
    implementation(gradleApi())
  }
}


gradlePlugin {
  plugins {
    create("propertiesPlugin") {
      id = "org.danbrough.kotlinxtras.properties"
      implementationClass = "org.danbrough.kotlinxtras.PropertiesPlugin"
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