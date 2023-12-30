package mqttMultiplatform

import arrow.core.Either
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import mqttprotocol.Entity
import mqttprotocol.MqttProtocol
import mqttprotocol.ProtocolError

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

    "should write to channel successfully" {
        // Arrange
        mqttProtocol.setupChannel(sourceEntity, destinationEntity)
        val message = "Test message".toByteArray()

        // Act
        val result = mqttProtocol.writeToChannel(sourceEntity, destinationEntity, message)

        // Assert
        result shouldBe Either.Right(Unit)
    }

    "should fail to write to channel when entities are not registered" {
        // Arrange
        val invalidSourceEntity = Entity("invalidSource")
        val invalidDestinationEntity = Entity("invalidDestination")
        val message = "Test message".toByteArray()

        // Act
        val result = mqttProtocol.writeToChannel(invalidSourceEntity, invalidDestinationEntity, message)

        // Assert
        result shouldBe Either.Left(ProtocolError.EntityNotRegistered(invalidDestinationEntity))
    }

    "should read from channel successfully" {
        // Arrange
        mqttProtocol.setupChannel(sourceEntity, destinationEntity)
        val message = "Test message".toByteArray()
        mqttProtocol.writeToChannel(sourceEntity, destinationEntity, message)

        // Act
        val flowResult = mqttProtocol.readFromChannel(sourceEntity, destinationEntity)

        // Assert
        flowResult shouldBe Either.Right(flowOf(message))
    }

    "should fail to read from channel when entities are not registered" {
        // Arrange
        val invalidSourceEntity = Entity("invalidSource")
        val invalidDestinationEntity = Entity("invalidDestination")

        // Act
        val flowResult = mqttProtocol.readFromChannel(invalidSourceEntity, invalidDestinationEntity)

        // Assert
        flowResult shouldBe Either.Left(ProtocolError.EntityNotRegistered(invalidSourceEntity))
    }

    "should initialize and finalize successfully" {
        // Act
        val initResult = mqttProtocol.initialize()
        val finalizeResult = mqttProtocol.finalize()

        // Assert
        initResult shouldBe Either.Right(Unit)
        finalizeResult shouldBe Either.Right(Unit)
    }
})
