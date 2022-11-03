# KotlinXtras

Common Kotlin packages with support for linux_arm32_hfp, linux_arm64 and Android native.


## Description

This repo is intended to serve as a hub for tweaking common kotlin libraries for unsupported platforms.
In particular the non linuxX64 platforms get neglected as well as the android native targets.
Precompiled versions will be published at `mavenCentral()` under [my repository](https://repo.maven.apache.org/maven2/org/danbrough/).  
They might take a while to get there so pre-releases can be found in [the sonatype staging section](https://s01.oss.sonatype.org/content/groups/staging/org/danbrough/)  
So you don't need to download this project and compile it, unless you want to.  

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
  implementation("org.danbrough.kotlinx:kotlinx-serialization-core:1.3.3")
  // ..etc 
}
```

The tweaked repos will be git submodules under the [repos](./repos) folder.
So you can clone those, compare with upstream, roll your own.
Due to the need for absolute hard-coded paths in cinterop def files this repo will need to be checked out at /usr/local/kotlinxtras
for building custom packages.  
You can also use the "consumer" plugin from this project to automatically download precompiled binaries to link against.  
See the sqlite,curl_standalone,sqldelight in the [demos](./demos) folder for example.


## News


The [sqldelight](./demos/sqldelight) demo is working with sqldelight 2.0.0-alpha04 with all linux and android native targets.









