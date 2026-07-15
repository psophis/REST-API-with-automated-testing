package com.bank.client.persistence

import com.bank.client.domain.Client
import com.bank.client.domain.ClientAddress
import com.bank.client.domain.ClientName
import org.assertj.core.api.Assertions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ContextConfiguration
import java.util.UUID
import kotlin.test.Test

@DataJpaTest
@Import(ClientRepositoryImpl::class)
@ContextConfiguration(classes = [ClientRepositoryIntegrationTest.JpaTestConfig::class])
class ClientRepositoryIntegrationTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("com.bank.client.persistence")
    @EnableJpaRepositories("com.bank.client.persistence")
    class JpaTestConfig

    @Autowired
    private lateinit var clientRepository: ClientRepositoryImpl

    @Autowired
    private lateinit var clientJpaRepository: ClientJpaRepository

    @Test
    fun `should load client by id`() {
        // Arrange
        val client = client()
        clientRepository.createClient(client)

        // Act
        val loaded = clientRepository.getClientById(client.id)

        // Assert
        Assertions.assertThat(loaded).isEqualTo(client)
    }

    @Test
    fun `should delete client by id`() {
        // Arrange
        val client = client()
        clientRepository.createClient(client)

        // Act
        clientRepository.deleteClientById(client.id)

        // Assert
        Assertions.assertThat(clientRepository.getClientById(client.id)).isNull()
        Assertions.assertThat(clientJpaRepository.findById(client.id)).isEmpty
    }

    @Test
    fun `should throw when client does not exist`() {
        // Arrange
        val clientId = "missing-client"

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(NoSuchElementException::class.java) {
            clientRepository.deleteClientById(clientId)
        }
    }

    @Test
    fun `should update client`() {
        // Arrange
        val client = client()
        clientRepository.createClient(client)
        val updatedClient = client.copy(name = ClientName("Smith", "Jane"))

        // Act
        val result = clientRepository.updateClient(updatedClient)

        // Assert
        Assertions.assertThat(result).isEqualTo(updatedClient)
    }

    private fun client(
        id: String = UUID.randomUUID().toString(),
        name: ClientName = ClientName("Doe", "John"),
        address: ClientAddress = ClientAddress("Main Street", "123", "12345", "Berlin"),
    ) = Client(
        id = id,
        name = name,
        address = address,
    )
}