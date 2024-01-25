plugins {
  `kotlin-dsl`
  `maven-publish`
}

group = xtras.versions.xtrasPackage.get()
version = xtras.versions.xtras.version.get()

repositories {
  maven("https://maven.danbrough.org")
  mavenCentral()
}

dependencies {
  implementation(xtras.kotlin.gradle.plugin)
  implementation(xtras.dokka.gradle.plugin)
  api(gradleApi())
  api(xtras.org.danbrough.klog)
}


publishing {
  repositories {
    val xtrasMavenDir = if (hasProperty("xtras.dir.maven"))
      File(property("xtras.dir.maven").toString())
    else if (hasProperty("xtras.dir"))
      File(property("xtras.dir").toString()).resolve("maven")
    else error("Neither xtras.dir.maven or xtras.dir are set")
    maven(xtrasMavenDir) {
      name = "Xtras"
    }
  }
}
/*
gradlePlugin {
  plugins {
    create("sonatype") {
      id = "$group.sonatype"
      implementationClass = "$group.sonatype.SonatypePlugin"
      displayName = "Sonatype plugin"
      description = "Sonatype publishing support"
    }
  }
}
*/

/*
gradlePlugin {
  plugins {
    create("support") {
      id = "$group.support"
      implementationClass = "$group.XtrasSupportPlugin"
    }
  }
}

*/
