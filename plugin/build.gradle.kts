plugins {
  `kotlin-dsl`
  `maven-publish`
  id("org.jetbrains.dokka")
  xtras("sonatype")
}

repositories {
  mavenCentral()
}


dependencies {
  compileOnly(kotlin("gradle-plugin"))
  compileOnly(kotlin("gradle-plugin-api"))
  compileOnly("org.jetbrains.dokka:dokka-gradle-plugin:_")
}



gradlePlugin {

  plugins {

    create("binariesPlugin") {
      id = "${group}.binaries"
      implementationClass = "$group.binaries.BinaryPlugin"
      displayName = "Xtras Binaries Plugin"
      description = "Provides native library support to Kotlin applications"
    }

    create("sonatypePlugin") {
      id = "$group.sonatype"
      implementationClass = "$group.sonatype.SonatypePlugin"
      displayName = "Sonatype plugin"
      description = "Sonatype publishing support"
    }

    create("core") {
      id = "$group.core"
      implementationClass = "$group.core.CorePlugin"
      displayName = "KotlinXtras core plugins"
      description = "Provides some core plugins"
    }

  }
}



afterEvaluate {


  extensions.findByType(SigningExtension::class.java)?.run {
    listOf("Xtras", "SonaType").forEach {


      tasks.findByPath("publishBinariesPluginPluginMarkerMavenPublicationTo${it}Repository")
        ?.mustRunAfter("signPluginMavenPublication")

      tasks.findByPath("publishPluginMavenPublicationTo${it}Repository")
        ?.mustRunAfter("signBinariesPluginPluginMarkerMavenPublication")

      tasks.findByPath("publishBinariesPluginPluginMarkerMavenPublicationTo${it}Repository")
        ?.mustRunAfter("signSonatypePluginPluginMarkerMavenPublication")

      tasks.findByPath("publishPluginMavenPublicationTo${it}Repository")
        ?.mustRunAfter("signSonatypePluginPluginMarkerMavenPublication")

      tasks.findByPath("publishSonatypePluginPluginMarkerMavenPublicationTo${it}Repository")
        ?.mustRunAfter("signPluginMavenPublication")

      tasks.findByPath("publishSonatypePluginPluginMarkerMavenPublicationTo${it}Repository")
        ?.mustRunAfter("signBinariesPluginPluginMarkerMavenPublication")

    }
  }
}

