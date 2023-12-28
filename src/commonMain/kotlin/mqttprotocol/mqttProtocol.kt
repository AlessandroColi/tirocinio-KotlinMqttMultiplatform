package mqttprotocol;


import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import mqttprotocol.Protocol

/**
 * Represents the MQTT protocol.
 */
expect class MqttProtocol(
    host: String = "localhost",
    port: Int = 1883,
    username: String? = null,
    password: String? = null,
    coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : Protocol