import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream

plugins {
  alias(libs.plugins.kotlinMultiplatform) apply false
  alias(libs.plugins.org.jetbrains.dokka) apply false


  `maven-publish`
}




val publishingVersion: String = libs.versions.xtrasPublishing.get()
val publishingGroup: String = libs.versions.xtrasPackage.get()


allprojects {

  group = publishingGroup
  version = publishingVersion

  apply<MavenPublishPlugin>()

  repositories {
    maven(rootProject.layout.buildDirectory.dir("xtras/maven"))
    maven("https://s01.oss.sonatype.org/content/groups/staging/")
    mavenCentral()
  }

  publishing {
    repositories {
      maven(rootProject.layout.buildDirectory.dir("xtras/maven")){
        name = "xtras"
      }
    }
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
  afterEvaluate {
    extensions.findByType(PublishingExtension::class)?.apply {
      publications.all {
        if (this is MavenPublication)
          xtrasPom()
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
fun MavenPublication.xtrasPom() {
  pom {

    name.set("KotlinXtras")
    description.set("Common kotlin packages with linux arm and android native support")
    url.set("https://github.com/danbrough/kotlinxtras/")

    licenses {
      license {
        name.set("Apache-2.0")
        url.set("https://opensource.org/licenses/Apache-2.0")
      }
    }

    scm {
      connection.set("scm:git:git@github.com:danbrough/kotlinxtras.git")
      developerConnection.set("scm:git:git@github.com:danbrough/kotlinxtras.git")
      url.set("https://github.com/danbrough/kotlinxtras/")
    }

    issueManagement {
      system.set("GitHub")
      url.set("https://github.com/danbrough/kotlinxtras/issues")
    }

    developers {
      developer {
        id.set("danbrough")
        name.set("Dan Brough")
        email.set("dan@danbrough.org")
        organizationUrl.set("https://github.com/danbrough")
      }
    }
  }
}





