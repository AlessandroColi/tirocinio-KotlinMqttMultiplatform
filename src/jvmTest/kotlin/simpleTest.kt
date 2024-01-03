package mqttMultiplatform

import arrow.core.Either
import arrow.core.raise.either
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.assertions.nondeterministic.eventuallyConfig
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import mqttprotocol.Entity
import mqttprotocol.MqttProtocol
import mqttprotocol.ProtocolError
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration
object ProjectConfig : AbstractProjectConfig() {
    override val timeout = 10.seconds
}
@OptIn(ExperimentalCoroutinesApi::class)
class CommunicatorTest : StringSpec({

    val host = "localhost"
    val port = 1883
    val username = "user"
    val password = "password"

    val sourceEntity = Entity("source")
    val destinationEntity = Entity("destination")

    val mqttProtocol = MqttProtocol(
        host = host,
        port = port,
        username = username,
        password = password,
    )


    "should initialize successfully" {
        val initResult = mqttProtocol.initialize()
        initResult shouldBe Either.Right(Unit)
    }

    "should fail to write to channel when entities are not registered" {
        val invalidSourceEntity = Entity("invalidSource")
        val invalidDestinationEntity = Entity("invalidDestination")
        val result = mqttProtocol.writeToChannel(invalidSourceEntity, invalidDestinationEntity, "error Test".toByteArray())
        result shouldBe Either.Left(ProtocolError.EntityNotRegistered(invalidDestinationEntity))
    }

    "should fail to read from channel when entities are not registered" {
        val invalidSourceEntity = Entity("invalidSource")
        val invalidDestinationEntity = Entity("invalidDestination")
        val flowResult = mqttProtocol.readFromChannel(invalidSourceEntity, invalidDestinationEntity)
        flowResult shouldBe Either.Left(ProtocolError.EntityNotRegistered(invalidSourceEntity))
    }

    "should work correctly" {
        val message = "protocol Test"
        var read = "initialized"
        mqttProtocol.setupChannel(sourceEntity, destinationEntity)
        val reciveJob = launch(UnconfinedTestDispatcher()) {
            println("started")
            val resultCollect = either {
                val flowResult = mqttProtocol.readFromChannel(sourceEntity, destinationEntity).bind()
                println("reading")
                flowResult.take(1).collect {
                    read = it.decodeToString()
                    println("read")
                }
            }
            resultCollect shouldBe Either.Right(Unit)
            println("left")
        }
//            delay(1000)
        println("write")
        val result = mqttProtocol.writeToChannel(sourceEntity, destinationEntity, message.toByteArray())
        result shouldBe Either.Right(Unit)

        reciveJob.join()
        read shouldBe message
    }


    "should finalize successfully" {
        val finalizeResult = mqttProtocol.finalize()
        finalizeResult shouldBe Either.Right(Unit)
    }
})
