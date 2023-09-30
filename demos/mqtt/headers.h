#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "MQTTClient.h"

#define ADDRESS     "tcp://mqtt.eclipseprojects.io:1883"
#define CLIENTID    "ExampleClientSub"
#define TOPIC       "MQTT Examples"
#define PAYLOAD     "Hello World!!"
#define QOS         1
#define TIMEOUT     10000L
volatile MQTTClient_deliveryToken deliveredtoken;

typedef void  (*ConnectionOptionsConf)(MQTTClient_connectOptions *);

MQTTClient_connectOptions connectOptions(ConnectionOptionsConf conf) {
  MQTTClient_connectOptions connOpts = MQTTClient_connectOptions_initializer;
  if (conf != NULL)
    conf(&connOpts);
  return connOpts;
}


void delivered(void *context, MQTTClient_deliveryToken dt) {
  printf("Message with token value %d delivery confirmed\n", dt);
  deliveredtoken = dt;
}

void testConnOptions(MQTTClient_connectOptions opts) {
  printf("CONN OPTIONS KEEY ALIVE: %d\n", opts.keepAliveInterval);
}

void testConnOptions2(MQTTClient_connectOptions *opts) {
  printf(
      "########################################################### CONN OPTIONS2 KEEY ALIVE: %d\n",
      opts->keepAliveInterval);
}

int msgarrvd(void *context, char *topicName, int topicLen, MQTTClient_message *message) {
  printf("Message arrived\n");
  printf("     topic: %s\n", topicName);
  printf("   message: %.*s\n", message->payloadlen, (char *) message->payload);
  MQTTClient_freeMessage(&message);
  MQTTClient_free(topicName);
  return 1;
}

void connlost(void *context, char *cause) {
  printf("\nConnection lost\n");
  printf("     cause: %s\n", cause);
}

int setupCallbacks(MQTTClient client) {
  return MQTTClient_setCallbacks(client, NULL, connlost, msgarrvd, delivered);
}


MQTTClient createClient() {
  MQTTClient client;
  int rc = 0;
  if ((rc = MQTTClient_create(&client, ADDRESS, CLIENTID, MQTTCLIENT_PERSISTENCE_NONE, NULL)) !=
      MQTTCLIENT_SUCCESS) {
    printf("Failed to create client, return code %d\n", rc);
    rc = EXIT_FAILURE;
  }
  return client;
}