package demo.subscribe

import demo.log
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.CPointerVarOf
import kotlinx.cinterop.CValue
import kotlinx.cinterop.CVariable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.getRawPointer
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.staticCFunction
import kotlinx.cinterop.toKString
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import mqtt.*
import platform.posix.EXIT_FAILURE
import platform.posix.exit


const val clientID = "ExampleMQTTPublisher"
const val mqttAddress = "tcp://mqtt.eclipseprojects.io:1883"

@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
  log.info("running demo1")

  memScoped {
    //val client: MQTTClient = createClient()!!.reinterpret()

    val conf = staticCFunction<CPointer<MQTTClient_connectOptions>?, Unit> {
      it?.pointed?.apply {
        log.trace("configuring MQTTClient_connectOptions")
        keepAliveInterval = 21
        cleansession = 1
      }
    }

    val connOptions: CValue<MQTTClient_connectOptions> =
      connectOptions(conf)


    log.trace("created connOptions")

    testConnOptions2(connOptions)

    //val clientVar = alloc<MQTTClientVar>()
//    val client = cValue<MQTTClientVar>()
//
//    MQTTClient_create(
//      client.ptr,
//      ADDRESS,
//      CLIENTID,
//      MQTTCLIENT_PERSISTENCE_NONE,
//      null
//    ).also {
//      if (it != MQTTCLIENT_SUCCESS) error("MQTTClient_create failed: $it")
//    }


    //val client: MQTTClient = createClient()!!

    val client = alloc<MQTTClientVar>()
    MQTTClient_create(
      client.ptr.getPointer(this).reinterpret(),
      ADDRESS,
      CLIENTID,
      MQTTCLIENT_PERSISTENCE_NONE,
      null
    ).also {
      if (it != MQTTCLIENT_SUCCESS) error("MQTTClient_create failed: $it")
    }

    log.trace("Created client, setting up callbacks")
    setupCallbacks(client.value!!.getPointer(this)).also {
      if (it != MQTTCLIENT_SUCCESS) error("setupCallbacks failed: $it")
    }

    log.trace("calling connect")
    MQTTClient_connect(client.value!!.getPointer(this), connOptions).also {
      if (it != MQTTCLIENT_SUCCESS) error("MQTTClient_connect failed: $it")
    }

    log.trace("all done")

    /*

    conn_opts.keepAliveInterval = 20;
    conn_opts.cleansession = 1;
    if ((rc = MQTTClient_connect(client, &conn_opts)) != MQTTCLIENT_SUCCESS)
    {
        printf("Failed to connect, return code %d\n", rc);
        exit(EXIT_FAILURE);
    }
     */
  }


}

