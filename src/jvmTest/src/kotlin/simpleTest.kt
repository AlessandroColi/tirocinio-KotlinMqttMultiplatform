package mqttMultiplatform.protocol
import arrow.core.Left
import arrow.core.Right
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.core.spec.style.scopes.DescribeScope
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking

@DescribeSpec
class MqttProtocolTest : DescribeSpec({

    describe("MqttProtocol") {

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
            coroutineDispatcher = testCoroutineDispatcher
        )

        it("should set up channel successfully") {
            runBlocking {
                // Act
                mqttProtocol.setupChannel(sourceEntity, destinationEntity)

                // Assert
                mqttProtocol.registeredTopics.size shouldBe 2
                mqttProtocol.topicChannels.size shouldBe 2
            }
        }

        it("should write to channel successfully") {
            runBlocking {
                // Arrange
                mqttProtocol.setupChannel(sourceEntity, destinationEntity)
                val message = "Test message".toByteArray()

                // Act
                val result = mqttProtocol.writeToChannel(sourceEntity, destinationEntity, message)

                // Assert
                result shouldBe Right(Unit)
            }
        }

        it("should fail to write to channel when entities are not registered") {
            runBlocking {
                // Arrange
                val invalidSourceEntity = Entity("invalidSource")
                val invalidDestinationEntity = Entity("invalidDestination")
                val message = "Test message".toByteArray()

                // Act
                val result = mqttProtocol.writeToChannel(invalidSourceEntity, invalidDestinationEntity, message)

                // Assert
                result shouldBe Left(ProtocolError.EntityNotRegistered(invalidDestinationEntity))
            }
        }

        it("should read from channel successfully") {
            runBlocking {
                // Arrange
                mqttProtocol.setupChannel(sourceEntity, destinationEntity)
                val message = "Test message".toByteArray()
                mqttProtocol.writeToChannel(sourceEntity, destinationEntity, message)

                // Act
                val flowResult = mqttProtocol.readFromChannel(sourceEntity, destinationEntity)

                // Assert
                flowResult shouldBe Right(flowOf(message))
            }
        }

        it("should fail to read from channel when entities are not registered") {
            runBlocking {
                // Arrange
                val invalidSourceEntity = Entity("invalidSource")
                val invalidDestinationEntity = Entity("invalidDestination")

                // Act
                val flowResult = mqttProtocol.readFromChannel(invalidSourceEntity, invalidDestinationEntity)

                // Assert
                flowResult shouldBe Left(ProtocolError.EntityNotRegistered(invalidSourceEntity))
            }
        }

        it("should initialize and finalize successfully") {
            runBlocking {
                // Act
                val initResult = mqttProtocol.initialize()
                val finalizeResult = mqttProtocol.finalize()

                // Assert
                initResult shouldBe Right(Unit)
                finalizeResult shouldBe Right(Unit)
            }
        }
    }
})

private fun DescribeScope.flowOf(vararg elements: ByteArray): Flow<ByteArray> {
    return kotlinx.coroutines.flow.flow {
        elements.forEach { emit(it) }
    }
}
