package org.danbrough.xtras.mqtt

import org.danbrough.xtras.XtrasDSLMarker
import org.danbrough.xtras.androidLibDir
import org.danbrough.xtras.env.cygpath
import org.danbrough.xtras.library.XtrasLibrary
import org.danbrough.xtras.library.xtrasCreateLibrary
import org.danbrough.xtras.library.xtrasRegisterSourceTask
import org.danbrough.xtras.log
import org.danbrough.xtras.source.gitSource
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.utils.`is`
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget


object Mqtt {
  const val extensionName = "mqtt"
  const val sourceURL = "https://github.com/eclipse/paho.mqtt.c.git"
  const val version = "1.3.12"
  const val commit = "v1.3.12"
}

class MQTTPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.log("MQTTPlugin.apply()")
  }
}


@XtrasDSLMarker
fun Project.xtrasMQTT(
  ssl: XtrasLibrary,
  name: String = Mqtt.extensionName,
  version: String = properties.getOrDefault("mqtt.version", Mqtt.version).toString(),
  commit: String = properties.getOrDefault("mqtt.commit", Mqtt.commit).toString(),
  configure: XtrasLibrary.() -> Unit = {},
) = xtrasCreateLibrary(name, version, ssl) {

  gitSource(Mqtt.sourceURL, commit)

  cinterops {
    interopsPackage = "mqtt"
    headers = """
//      #staticLibraries =  libcrypto.a libssl.a
      headers = MQTTClient.h  MQTTClientPersistence.h  MQTTExportDeclarations.h  MQTTProperties.h  MQTTReasonCodes.h  MQTTSubscribeOpts.h

      linkerOpts.linux = -lpaho-mqtt3cs
//      linkerOpts.linux = -ldl -lc -lm -lssl -lcrypto
//      linkerOpts.android = -ldl -lc -lm -lssl -lcrypto
//      linkerOpts.macos = -ldl -lc -lm -lssl -lcrypto
//      linkerOpts.mingw = -lm -lssl -lcrypto
//      compilerOpts.android = -D__ANDROID_API__=21
//      compilerOpts =  -Wno-macro-redefined -Wno-deprecated-declarations  -Wno-incompatible-pointer-types-discards-qualifiers
//      #compilerOpts = -static
//
          """.trimIndent()

    headersSourceCode = """
      #include <stdio.h>
      #include <stdlib.h>
      #include <string.h>
      #include "MQTTClient.h"
      
      #define ADDRESS     "tcp://mqtt.eclipseprojects.io:1883"
      #define CLIENTID    "ExampleClientSub"
      #define TOPIC       "MQTT Examples"
      #define PAYLOAD     "Hello World!"
      #define QOS         1
      #define TIMEOUT     10000L
      volatile MQTTClient_deliveryToken deliveredtoken;
      
      MQTTClient_connectOptions connectOptions() { return (MQTTClient_connectOptions) MQTTClient_connectOptions_initializer; }      
      

      void delivered(void *context, MQTTClient_deliveryToken dt)
      {
          printf("Message with token value %d delivery confirmed\n", dt);
          deliveredtoken = dt;
      }

      int msgarrvd(void *context, char *topicName, int topicLen, MQTTClient_message *message)
      {
          printf("Message arrived\n");
          printf("     topic: %s\n", topicName);
          printf("   message: %.*s\n", message->payloadlen, (char*)message->payload);
          MQTTClient_freeMessage(&message);
          MQTTClient_free(topicName);
          return 1;
      }

      void connlost(void *context, char *cause)
      {
          printf("\nConnection lost\n");
          printf("     cause: %s\n", cause);
      }
      
      void setupCallbacks(MQTTClient client){
          int rc = 0;
          printf("inside setupCallbacks(): %d\n",client);
          if ((rc = MQTTClient_setCallbacks(client, NULL, connlost, msgarrvd, delivered)) != MQTTCLIENT_SUCCESS){
              printf("Failed to set callbacks, return code %d\n", rc);
              rc = EXIT_FAILURE;
          }
      }
      
      
      MQTTClient createClient(){
        MQTTClient client;
        int rc = 0;
        if ((rc = MQTTClient_create(&client, ADDRESS, CLIENTID,MQTTCLIENT_PERSISTENCE_NONE, NULL)) != MQTTCLIENT_SUCCESS)
        {
            printf("Failed to create client, return code %d\n", rc);
            rc = EXIT_FAILURE;
        }
        return client;
      }


    """.trimIndent()
  }

  configure()


  supportedTargets.forEach { target ->
    val compileDir = sourcesDir(target).resolve("build")
    val configureTask = xtrasRegisterSourceTask(XtrasLibrary.TaskName.CONFIGURE, target) {
      dependsOn(libraryDeps.map { it.extractArchiveTaskName(target) })
      doFirst {
        if (compileDir.exists()) {
          compileDir.deleteRecursively()
        }
        compileDir.mkdirs()
      }
      workingDir(compileDir)
      outputs.file(compileDir.resolve("Makefile"))

      /*

OPTS="-DPAHO_BUILD_DOCUMENTATION=FALSE -DPAHO_BUILD_SAMPLES=TRUE -DCMAKE_MINIMUM_REQUIRED_VERSION=2.8 \
-DCMAKE_INSTALL_PREFIX=$PREFIX \
-DANDROID_ABI=$ANDROID_ABI \
-DCMAKE_TOOLCHAIN_FILE=$NDK/build/cmake/android.toolchain.cmake -DANDROID_PLATFORM=21 \
-DPAHO_ENABLE_TESTING=FALSE -DPAHO_WITH_SSL=TRUE \
-DOPENSSL_CRYPTO_LIBRARY=$OPENSSL/lib/libcrypto.so \
-DOPENSSL_SSL_LIBRARY=$OPENSSL/lib/libssl.so  \
-DOPENSSL_ROOT_DIR=$OPENSSL  \
 -DOPENSSL_LIBRARIES=$OPENSSL/lib -DOPENSSL_INCLUDE_DIR=$OPENSSL/include "
       */
      val cmakeArgs = mutableListOf(
        buildEnvironment.binaries.cmake,
        "-G", "Unix Makefiles",
        "-DCMAKE_INSTALL_PREFIX=${buildDir(target).cygpath(buildEnvironment)}",
        "-DPAHO_WITH_SSL=TRUE",
        "-DPAHO_BUILD_STATIC=TRUE",
        "-DPAHO_ENABLE_TESTING=FALSE",
        "-DPAHO_BUILD_SAMPLES=TRUE",
        "-DOPENSSL_ROOT_DIR=${ssl.libsDir(target).cygpath(buildEnvironment)}",
      )

      if (target.family == Family.ANDROID) {
        cmakeArgs += listOf(
          "-DANDROID_ABI=${target.androidLibDir}",
          "-DANDROID_PLATFORM=21",
          "-DCMAKE_TOOLCHAIN_FILE=${
            buildEnvironment.androidNdkDir.resolve("build/cmake/android.toolchain.cmake")
              .cygpath(buildEnvironment)
          }",
          "-DOPENSSL_SSL_LIBRARY=${
            ssl.libsDir(target).resolve("lib/libssl.so").cygpath(buildEnvironment)
          }",
          "-DOPENSSL_CRYPTO_LIBRARY=${
            ssl.libsDir(target).resolve("lib/libcrypto.so").cygpath(buildEnvironment)
          }",
          "-DOPENSSL_INCLUDE_DIR=${
            ssl.libsDir(target).resolve("include").cygpath(buildEnvironment)
          }",
          "-DOPENSSL_LIBRARIES=${
            ssl.libsDir(target).resolve("lib").cygpath(buildEnvironment)
          }",
        )
      } else if (target.family.isAppleFamily) {
        if (target == KonanTarget.MACOS_X64)
          cmakeArgs += "-DCMAKE_OSX_ARCHITECTURES=x86_64"
        else if (target == KonanTarget.MACOS_ARM64)
          cmakeArgs += "-DCMAKE_OSX_ARCHITECTURES=arm64"
      }

      cmakeArgs += ".."

      commandLine(cmakeArgs)
    }

    xtrasRegisterSourceTask(XtrasLibrary.TaskName.BUILD, target) {
      dependsOn(configureTask)
      workingDir(compileDir)
      commandLine(buildEnvironment.binaries.make, "install")
    }
  }
}

