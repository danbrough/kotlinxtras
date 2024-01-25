#ifndef __KMQTT_ASYNC_H__
#define __KMQTT_ASYNC_H__

#include <stdio.h>
#include "MQTTAsync.h"

/** Initializer for connect options for MQTT 3.1.1 non-WebSocket connections */
static inline MQTTAsync_connectOptions
connectOptionsAsync() { return (MQTTAsync_connectOptions) MQTTAsync_connectOptions_initializer; }


/** Initializer for connect options for MQTT 5.0 non-WebSocket connections */
static inline MQTTAsync_connectOptions
connectOptionsAsync5() { return (MQTTAsync_connectOptions) MQTTAsync_connectOptions_initializer5; }

/** Initializer for connect options for MQTT 3.1.1 WebSockets connections.
  * The keepalive interval is set to 45 seconds to avoid webserver 60 second inactivity timeouts.
  */
static inline MQTTAsync_connectOptions
connectOptionsAsyncWS() { return (MQTTAsync_connectOptions) MQTTAsync_connectOptions_initializer_ws; }


/** Initializer for connect options for MQTT 5.0 WebSockets connections.
  * The keepalive interval is set to 45 seconds to avoid webserver 60 second inactivity timeouts.
  */
static inline MQTTAsync_connectOptions
connectOptionsAsyncWS5() { return (MQTTAsync_connectOptions) MQTTAsync_connectOptions_initializer5_ws; }

static inline MQTTAsync_callOptions callOptions() {
  return (MQTTAsync_callOptions) MQTTAsync_responseOptions_initializer;
}


static inline MQTTAsync_SSLOptions
sslOptions() { return (MQTTAsync_SSLOptions) MQTTAsync_SSLOptions_initializer; }


#endif
