import org.danbrough.kotlinxtras.XTRAS_PACKAGE
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.danbrough.kotlinxtras.xtrasPom
import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
  alias(libs.plugins.kotlinMultiplatform) apply false
  `maven-publish`
  alias(libs.plugins.org.jetbrains.dokka) apply false
  id("org.danbrough.kotlinxtras.xtras")
}

println("Using Kotlin compiler version: ${KotlinCompilerVersion.VERSION}")

val publishingVersion: String = libs.versions.kotlinXtrasPublishing.get()


version = publishingVersion

allprojects {

  group = XTRAS_PACKAGE
  version = publishingVersion

  repositories {

    maven(rootProject.layout.buildDirectory.dir("xtras/maven"))
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
  }

  tasks.withType<AbstractTestTask> {
    testLogging {
      events = setOf(
        TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED
      )
      exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
      showStandardStreams = true
      showStackTraces = true
    }
    outputs.upToDateWhen {
      false
    }
  }
}



subprojects {
  group = XTRAS_PACKAGE

  afterEvaluate {

/*
    extensions.findByType(JavaPluginExtension::class.java)?.apply {
      toolchain.languageVersion.set(JavaLanguageVersion.of(Xtras.javaLangVersion))
    }


    extensions.findByType(org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension::class.java)
      ?.apply {
        jvmToolchain {
          languageVersion.set(JavaLanguageVersion.of(Xtras.javaLangVersion))
        }
      }

*/

    extensions.findByType(PublishingExtension::class)?.also {
      publishing {
        publications.all {
          if (this !is MavenPublication) return@all
          xtrasPom()
        }
      }
    }
  }
}


/*
afterEvaluate {
  tasks.findByPath(":plugin:javadocJar")?.also {
    tasks.findByPath(":core:dokkaHtml")?.mustRunAfter(it)
  }
}
*/
