package com.bank.client.persistence

import com.bank.client.domain.Client
import com.bank.client.domain.ClientAddress
import com.bank.client.domain.ClientName
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.Optional

class ClientRepositoryImplTest {
    private val clientJpaRepository = mockk<ClientJpaRepository>()
    private val clientRepository = ClientRepositoryImpl(clientJpaRepository)

    @Test
    fun `should get client by id`() {
        // Arrange
        val client = client()
        every { clientJpaRepository.findById(client.id) } returns Optional.of(entity(client))

        // Act
        val result = clientRepository.getClientById(client.id)

        // Assert
        assertThat(result).isEqualTo(client)
        verify(exactly = 1) { clientJpaRepository.findById(client.id) }
    }

    @Test
    fun `should return null when client is not found`() {
        // Arrange
        val clientId = "missing-client"
        every { clientJpaRepository.findById(clientId) } returns Optional.empty()

        // Act
        val result = clientRepository.getClientById(clientId)

        // Assert
        assertThat(result).isNull()
        verify(exactly = 1) { clientJpaRepository.findById(clientId) }
    }

    @Test
    fun `should create client`() {
        // Arrange
        val client = client()
        every { clientJpaRepository.save(any()) } returns entity(client)

        // Act
        val result = clientRepository.createClient(client)

        // Assert
        assertThat(result).isEqualTo(client)
        verify(exactly = 1) { clientJpaRepository.save(any()) }
    }

    @Test
    fun `should update client`() {
        // Arrange
        val client = client()
        every { clientJpaRepository.existsById(client.id) } returns true
        every { clientJpaRepository.save(any()) } returns entity(client)

        // Act
        val result = clientRepository.updateClient(client)

        // Assert
        assertThat(result).isEqualTo(client)
        verify(exactly = 1) { clientJpaRepository.existsById(client.id) }
        verify(exactly = 1) { clientJpaRepository.save(any()) }
    }

    @Test
    fun `should throw when updating missing client`() {
        // Arrange
        val client = client()
        every { clientJpaRepository.existsById(client.id) } returns false

        // Act
        val exception =
            assertThrows(NoSuchElementException::class.java) {
                clientRepository.updateClient(client)
            }

        // Assert
        assertThat(exception.message).isEqualTo("Client not found: ${client.id}")
        verify(exactly = 1) { clientJpaRepository.existsById(client.id) }
    }

    @Test
    fun `should delete client`() {
        // Arrange
        val clientId = "client-id"
        every { clientJpaRepository.existsById(clientId) } returns true
        every { clientJpaRepository.deleteById(clientId) } just runs

        // Act
        clientRepository.deleteClientById(clientId)

        // Assert
        verify(exactly = 1) { clientJpaRepository.existsById(clientId) }
        verify(exactly = 1) { clientJpaRepository.deleteById(clientId) }
    }

    @Test
    fun `should throw when deleting missing client`() {
        // Arrange
        val clientId = "missing-client"
        every { clientJpaRepository.existsById(clientId) } returns false

        // Act
        val exception =
            assertThrows(NoSuchElementException::class.java) {
                clientRepository.deleteClientById(clientId)
            }

        // Assert
        assertThat(exception.message).isEqualTo("Client not found: $clientId")
        verify(exactly = 1) { clientJpaRepository.existsById(clientId) }
    }

    private fun client(id: String = "client-id") =
        Client(
            id = id,
            name =
                ClientName(
                    name = "Doe",
                    firstName = "Jane",
                ),
            address =
                ClientAddress(
                    street = "Main Street",
                    number = "1",
                    zipCode = "12345",
                    city = "Berlin",
                ),
        )

    private fun entity(client: Client) =
        ClientEntity(
            id = client.id,
            lastName = client.name.name,
            firstName = client.name.firstName,
            street = client.address.street,
            number = client.address.number,
            city = client.address.city,
            zipCode = client.address.zipCode,
        )
}
