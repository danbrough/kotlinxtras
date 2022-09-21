# Curl demo 

This demo uses the org.danbrough.kotlinxtras:curl library to compile a curl demo.  
It expects precompiled curl and openssl at /usr/local/kotlinxtras/libs/curl,openssl but might
work with your system versions.  
See the (curl standalone)[../curl_standalone/] for this demo that downloads precompiled binaries to link against.

The cacert.pem file is from: http://curl.haxx.se/ca/cacert.pem

You need to specify it in the environment variable "CA_CERT_FILE" or run the demo with:  
```bash 

./gradlew  runDemo1DebugExecutableMacosX64  (loads example.com) 
# or 
./gradlew  runDemo1DebugExecutableMacosX64 -Purl=https://python.org
# or 
./gradlew  runDemo1DebugExecutableMacosArm64 -Purl=https://python.org
./gradlew  runDemo1DebugExecutableLinuxX64 -Purl=https://python.org

# or link the binary to run somewhere else:
./gradlew linkDemo1DebugExecutableLinuxArm32Hfp
./gradlew linkDemo1DebugExecutableAndroidArm32

```

