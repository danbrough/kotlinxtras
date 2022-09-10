# KotlinXtras

Common Kotlin packages with support for linux_arm32_hfp, linux_arm64 and Android native.


## Description

This repo is intended to serve as a hub for tweaking common kotlin libraries for unsupported platforms.
In particular the non linuxX64 platforms get neglected as well as the android native targets.
Precompiled versions will be published at `mavenCentral()` under [my repository](https://repo.maven.apache.org/maven2/org/danbrough/).
They might take a while to get there so pre-releases can be found in [the sonatype staging section](https://s01.oss.sonatype.org/content/groups/staging/org/danbrough/kipfs/).

To use them in your projects just add: 

```gradle 
repositories {
 // for the pre releases
  maven("https://s01.oss.sonatype.org/content/groups/staging/")
  // for the final releases
  mavenCentral()
}

//and change "org.jetbrains.kotlinx" to "org.danbrough.kotlinx"
dependencies {
  implementation("org.danbrough.kotlinx:kotlinx-coroutines-core:1.6.4")
  // ..etc 
}
```

The tweaked repos will be git submodules under the [repos](./repos) folder.
So you can clone those, compare with upstream, roll your own.
Due to the need for absolute hard-coded paths in cinterop def files this repo will need to be checked out at /usr/local/kotlinxtras
for building custom packages.
I plan to bundle the precompiled ssl/curl binaries into jars to make this simpler.
You could use those for compiling and the standard ssl/curl binaries on your target system for running.
Or you could redistribute them.
Static compilation will be another option.

## Status

Scripts are in place for cross compiling openssl and curl.
Okio,serialization,coroutines,atomic-fu,and kotlinx-datetime are working.
Ktor is in progress but I have got a demo running on linuxArm32Hfp using ktor-client-curl.






