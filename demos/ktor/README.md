# Ktor demo

This expects to find curl and openssl in the /usr/local/kotlinxtras/libs/ directory.
To build those run `./gradlew curl:buildLinuxX64 .. curl:buildLinuxArm32Hfp ..` in the root directory
that should be located at /usr/local/kotlinxtras

To compile run `./gradlew linkDemo1DebugExecutableLinuxArm32Hfp` (or linkDemo1DebugExecutableLinuxArm64) 
in this directory.

Or run the demo on your host platform:  

`
./gradlew runDemo1DebugExecutableLinuxX64
or
./gradlew runDemo1DebugExecutableMacosX64
or
./gradlew runDemo1DebugExecutableMacosArm64
`


