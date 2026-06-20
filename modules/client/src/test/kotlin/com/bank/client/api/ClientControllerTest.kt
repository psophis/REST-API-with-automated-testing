package com.bank.client.api

import com.bank.bankaccount.domain.BankAccount
import com.bank.client.application.ClientService
import com.bank.client.domain.Client
import com.bank.client.domain.ClientAddress
import com.bank.client.domain.ClientName
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.Instant

class ClientControllerTest {
    private val clientService = mockk<ClientService>()
    private val clientController = ClientController(clientService)

    @Test
    fun `should get client`() {
        // Arrange
        val client = client()
        every { clientService.getClient(client.id) } returns client

        // Act
        val response = clientController.getClient(client.id)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.id).isEqualTo(client.id)
        assertThat(response.body!!.name).isEqualTo(client.name)
        assertThat(response.body!!.address).isEqualTo(client.address)
        verify(exactly = 1) { clientService.getClient(client.id) }
    }

    @Test
    fun `should return 404 when client is not found`() {
        // Arrange
        val clientId = "missing-client"
        every { clientService.getClient(clientId) } throws NoSuchElementException("Client not found: $clientId")

        // Act
        val response = clientController.getClient(clientId)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        verify(exactly = 1) { clientService.getClient(clientId) }
    }

    @Test
    fun `should get client accounts`() {
        // Arrange
        val clientId = "client-id"
        val account = account(clientId)
        every { clientService.getClientAccounts(clientId) } returns listOf(account)

        // Act
        val response = clientController.getClientAccounts(clientId)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).hasSize(1)
        assertThat(response.body!![0].id).isEqualTo(account.id)
        assertThat(response.body!![0].clientId).isEqualTo(account.clientId)
        assertThat(response.body!![0].iban).isEqualTo(account.iban)
        verify(exactly = 1) { clientService.getClientAccounts(clientId) }
    }

    @Test
    fun `should return 404 when client accounts are not found`() {
        // Arrange
        val clientId = "missing-client"
        every { clientService.getClientAccounts(clientId) } throws NoSuchElementException("Client not found: $clientId")

        // Act
        val response = clientController.getClientAccounts(clientId)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        verify(exactly = 1) { clientService.getClientAccounts(clientId) }
    }

    @Test
    fun `should create client`() {
        // Arrange
        val request = clientDto(id = "request-id")
        val createdClient = client(id = "created-id")
        every { clientService.createClient(request) } returns createdClient

        // Act
        val response = clientController.createClient(request)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.id).isEqualTo(createdClient.id)
        assertThat(response.body!!.name).isEqualTo(createdClient.name)
        assertThat(response.body!!.address).isEqualTo(createdClient.address)
        verify(exactly = 1) { clientService.createClient(request) }
    }

    @Test
    fun `should update client`() {
        // Arrange
        val request = clientDto(id = "client-id")
        val updatedClient = client(id = request.id)
        every { clientService.updateClient(request) } returns updatedClient

        // Act
        val response = clientController.updateClient(request)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.id).isEqualTo(updatedClient.id)
        assertThat(response.body!!.name).isEqualTo(updatedClient.name)
        assertThat(response.body!!.address).isEqualTo(updatedClient.address)
        verify(exactly = 1) { clientService.updateClient(request) }
    }

    @Test
    fun `should delete client`() {
        // Arrange
        val clientId = "client-id"
        every { clientService.deleteClient(clientId) } just runs

        // Act
        val response = clientController.deleteClient(clientId)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        verify(exactly = 1) { clientService.deleteClient(clientId) }
    }

    @Test
    fun `should return 404 when deleting missing client`() {
        // Arrange
        val clientId = "missing-client"
        every { clientService.deleteClient(clientId) } throws NoSuchElementException("Client not found: $clientId")

        // Act
        val response = clientController.deleteClient(clientId)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        verify(exactly = 1) { clientService.deleteClient(clientId) }
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

    private fun clientDto(id: String = "client-id") =
        ClientUpdateRequest(
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

    private fun account(clientId: String) =
        BankAccount(
            id = "account-id",
            clientId = clientId,
            iban = "DE1234567890",
            balance = BigDecimal("100.00"),
            createdAt = Instant.now(),
        )
}
