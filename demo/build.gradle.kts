import BuildEnvironment.buildEnvironment
import BuildEnvironment.konanDepsTask
import BuildEnvironment.hostTriplet
import BuildEnvironment.platformName
import OpenSSL.opensslPlatform
import OpenSSL.opensslPrefix
import OpenSSL.opensslSrcDir
import org.gradle.configurationcache.extensions.capitalized
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import BuildEnvironment.declareNativeTargets

plugins {
  kotlin("multiplatform")
  `maven-publish`
}

kotlin {
  linuxX64()
  androidNativeX86()

  
}