import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  kotlin("multiplatform") apply false
}

ProjectProperties.init(project)

val message:String = ProjectProperties.getProjectProperty("message","no message")

tasks.create("thang"){
  doFirst {
    System.getProperties().keys.map { it.toString() }.filter { it.contains("message") }.forEach {
      println("FOUND SYS PROP: $it")
    }
    project.properties.keys.filter { it.contains("message") }.forEach {
      println("FOUND PROJECT PROPERTY: $it = ${project.properties[it]}")
    }
    println("org.gradle.project.message: ${project.properties["org.gradle.project.message"]}")
    println("project.message: ${project.properties["message"]}")
    println("The message is $message")


  }
}


allprojects {
  repositories {
    mavenCentral()
  }

}
