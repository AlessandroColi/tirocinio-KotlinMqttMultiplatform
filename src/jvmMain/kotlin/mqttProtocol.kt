package mqttprotocol

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.right
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.eclipse.paho.mqttv5.client.IMqttToken
import org.eclipse.paho.mqttv5.client.MqttAsyncClient
import org.eclipse.paho.mqttv5.client.MqttCallback
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.packet.MqttProperties

/**
 * MQTT Protocol implementation on JVM side.
 */
actual class MqttProtocol actual constructor(
    private val host: String,
    private val port: Int,
    private val username: String?,
    private val password: String?,
    private val coroutineDispatcher: CoroutineDispatcher,
) : Protocol {

    private val scope = CoroutineScope(coroutineDispatcher + Job())

    private val registeredTopics = mutableMapOf<Pair<Entity, Entity>, String>()
    private val topicChannels = mutableMapOf<String, MutableSharedFlow<ByteArray>>()
    private lateinit var mqttClient: MqttAsyncClient
    private lateinit var listenerJob: Job
    private val connectionOptions = MqttConnectionOptions().apply {
        isCleanStart = false
        userName = username
        password = this@MqttProtocol.password?.encodeToByteArray()
    }

    override suspend fun setupChannel(source: Entity, destination: Entity) {
        registeredTopics += (source to destination) to toTopics(source, destination)
        registeredTopics += (destination to source) to toTopics(destination, source)
        topicChannels += toTopics(source, destination) to MutableSharedFlow(1)
        topicChannels += toTopics(destination, source) to MutableSharedFlow(1)
    }

    override suspend fun writeToChannel(from: Entity, to: Entity, message: ByteArray): Either<ProtocolError, Unit> = coroutineScope {
        either {
            val topic = registeredTopics[Pair(from, to)]
            ensureNotNull(topic) { ProtocolError.EntityNotRegistered(to) }
            val mqttMessage = MqttMessage(message).apply { qos = 2 }
            async(coroutineDispatcher) {
                Either.catch { mqttClient.publish(topic, mqttMessage).waitForCompletion() }
                    .mapLeft { ProtocolError.ProtocolException(it) }
            }.await().bind()
        }
    }

    override fun readFromChannel(from: Entity, to: Entity): Either<ProtocolError, Flow<ByteArray>> = either {
        val candidateTopic = ensureNotNull(registeredTopics[Pair(from, to)]) { ProtocolError.EntityNotRegistered(from) }
        val channel = ensureNotNull(topicChannels[candidateTopic]) { ProtocolError.EntityNotRegistered(from) }
        channel.asSharedFlow()
    }

    suspend fun initialize(): Either<ProtocolError, Unit> = coroutineScope {
        either {
            Either.catch {
                mqttClient = MqttAsyncClient(
                    "tcp://$host:$port",
                    "MqttProtocol Test",
                    MemoryPersistence(),
                )
            }.mapLeft { ProtocolError.ProtocolException(it) }.bind()
            scope.async(coroutineDispatcher) {
                Either.catch { mqttClient.connect(connectionOptions).waitForCompletion() }
                    .mapLeft { ProtocolError.ProtocolException(it) }
            }.await().bind()
            listenerJob = scope.launch {
                val callback = object : MqttCallback {
                    override fun disconnected(disconnectResponse: MqttDisconnectResponse?) = Unit
                    override fun mqttErrorOccurred(exception: MqttException?) {
                    }

                    override fun messageArrived(topic: String?, message: MqttMessage?) {
                        val payload = message?.payload
                        requireNotNull(payload) { "Message cannot be null" }
                        topicChannels[topic]?.tryEmit(payload)
                    }

                    override fun deliveryComplete(token: IMqttToken?) = Unit
                    override fun connectComplete(reconnect: Boolean, serverURI: String?) = Unit
                    override fun authPacketArrived(reasonCode: Int, properties: MqttProperties?) = Unit
                }
                mqttClient.setCallback(callback)
                async { mqttClient.subscribe(arrayOf("MqttProtocol Test/+/+/+"), intArrayOf(1)).waitForCompletion() }.await()
            }
        }
    }

    suspend fun finalize(): Either<ProtocolError, Unit> {
        mqttClient.disconnect();
        mqttClient.close()
        scope.coroutineContext.cancelChildren()
        return Unit.right()
    }

    /**
     * Creates the MQTT topic for the given entity.
     * @return the topic where the entity will receive messages.
     */
    private fun toTopics(source: Entity, destination: Entity): String {
        return if (source.id != null && destination.id != null) {
            "MqttProtocol Test/${source.entityName}/${destination.entityName}/${destination.id}"
        } else {
            "MqttProtocol Test/${source.entityName}/${destination.entityName}"
        }
    }
}
