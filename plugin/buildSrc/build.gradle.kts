

plugins {
  `kotlin-dsl`
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(gradleApi())
  implementation(gradleKotlinDsl())
  implementation(kotlin("gradle-plugin", "1.7.20"))
}