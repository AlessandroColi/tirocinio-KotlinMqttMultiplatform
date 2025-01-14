package mqttprotocol;

import arrow.core.Either
import kotlinx.coroutines.flow.Flow

/**
 * Represents the entity that the [Protocol] is communicating with.
 * An [Entity] is characterized by a [entityName] and an optional [id].
 * The [id] is used to distinguish between multiple entities with the same [entityName].
 * The [data] is used to store additional information about the entity.
 */
data class Entity(val entityName: String, val id: String? = null, val metadata: Map<String, String> = emptyMap())

/**
 * Represents the low-level operations needed to communicate with another entity.
 * The communication is done through a channel that is set up by the [Protocol].
 */
interface Protocol{
    /**
     * Sets up the communication channel with the given [source].
     */
    suspend fun setupChannel(source: Entity, destination: Entity)

    /**
     * Writes the given [message] to the channel of the given [to] entity.
     */
    suspend fun writeToChannel(from: Entity, to: Entity, message: ByteArray): Either<ProtocolError, Unit>

    /**
     * Reads from the channel of the given [from] entity.
     */
    fun readFromChannel(from: Entity, to: Entity): Either<ProtocolError, Flow<ByteArray>>
    suspend fun initialize(): Either<ProtocolError, Unit>
    suspend fun finalize(): Either<ProtocolError, Unit>
}
