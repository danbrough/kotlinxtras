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

  }
}



/*    Possible solutions:
      1. Declare task ':plugin:signPluginMavenPublication' as an input of ':plugin:publishBinariesPluginPluginMarkerMavenPublicationToSonaTypeRepository'.
      2. Declare an explicit dependency on ':plugin:signPluginMavenPublication' from ':plugin:publishBinariesPluginPluginMarkerMavenPublicationToSonaTypeRepository' using Task#dependsOn.
      3. Declare an explicit dependency on ':plugin:signPluginMavenPublication' from ':plugin:publishBinariesPluginPluginMarkerMavenPublicationToSonaTypeRepository' using Task#mustRunAfter.

      1. Declare task ':plugin:signBinariesPluginPluginMarkerMavenPublication' as an input of ':plugin:publishPluginMavenPublicationToSonaTypeRepository'.
      2. Declare an explicit dependency on ':plugin:signBinariesPluginPluginMarkerMavenPublication' from ':plugin:publishPluginMavenPublicationToSonaTypeRepository' using Task#dependsOn.
      3. Declare an explicit dependency on ':plugin:signBinariesPluginPluginMarkerMavenPublication' from ':plugin:publishPluginMavenPublicationToSonaTypeRepository' using Task#mustRunAfter.

      1. Declare task ':plugin:signSonatypePluginPluginMarkerMavenPublication' as an input of ':plugin:publishBinariesPluginPluginMarkerMavenPublicationToSonaTypeRepository'.
      2. Declare an explicit dependency on ':plugin:signSonatypePluginPluginMarkerMavenPublication' from ':plugin:publishBinariesPluginPluginMarkerMavenPublicationToSonaTypeRepository' using Task#dependsOn.
      3. Declare an explicit dependency on ':plugin:signSonatypePluginPluginMarkerMavenPublication' from ':plugin:publishBinariesPluginPluginMarkerMavenPublicationToSonaTypeRepository' using Task#mustRunAfter.

      1. Declare task ':plugin:signSonatypePluginPluginMarkerMavenPublication' as an input of ':plugin:publishPluginMavenPublicationToSonaTypeRepository'.
      2. Declare an explicit dependency on ':plugin:signSonatypePluginPluginMarkerMavenPublication' from ':plugin:publishPluginMavenPublicationToSonaTypeRepository' using Task#dependsOn.
      3. Declare an explicit dependency on ':plugin:signSonatypePluginPluginMarkerMavenPublication' from ':plugin:publishPluginMavenPublicationToSonaTypeRepository' using Task#mustRunAfter.

      1. Declare task ':plugin:signBinariesPluginPluginMarkerMavenPublication' as an input of ':plugin:publishSonatypePluginPluginMarkerMavenPublicationToSonaTypeRepository'.
      2. Declare an explicit dependency on ':plugin:signBinariesPluginPluginMarkerMavenPublication' from ':plugin:publishSonatypePluginPluginMarkerMavenPublicationToSonaTypeRepository' using Task#dependsOn.
      3. Declare an explicit dependency on ':plugin:signBinariesPluginPluginMarkerMavenPublication' from ':plugin:publishSonatypePluginPluginMarkerMavenPublicationToSonaTypeRepository' using Task#mustRunAfter.


 */

afterEvaluate {

  tasks.getByName("publishBinariesPluginPluginMarkerMavenPublicationToSonaTypeRepository").dependsOn("signPluginMavenPublication")
  tasks.getByName("publishPluginMavenPublicationToSonaTypeRepository").dependsOn("signBinariesPluginPluginMarkerMavenPublication")
  tasks.getByName("publishBinariesPluginPluginMarkerMavenPublicationToSonaTypeRepository").dependsOn("signSonatypePluginPluginMarkerMavenPublication")
  tasks.getByName("publishPluginMavenPublicationToSonaTypeRepository").dependsOn("signSonatypePluginPluginMarkerMavenPublication")
  tasks.getByName("publishSonatypePluginPluginMarkerMavenPublicationToSonaTypeRepository").dependsOn("signBinariesPluginPluginMarkerMavenPublication")


}