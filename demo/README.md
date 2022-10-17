# Curl standalone demo

Rather than build KotlinXtras at /usr/local/kotlinxtras this demo shows how to use the precompiled binaries
from maven.  

```bash

./gradlew  runDemo1DebugExecutableLinuxX64 //for https://example.com or -Purl=https://...
./gradlew  runDemo1DebugExecutableMacosX64
./gradlew  runDemo1DebugExecutableMacosArm64
./gradlew  linkDemo1DebugExecutableLinuxArm32Hfp .. etc 

```



