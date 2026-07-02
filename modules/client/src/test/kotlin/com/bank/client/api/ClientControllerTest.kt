package com.bank.client.api

import com.bank.bankaccount.domain.BankAccount
import com.bank.client.application.ClientNotFoundException
import com.bank.client.application.ClientService
import com.bank.client.domain.Client
import com.bank.client.domain.ClientAddress
import com.bank.client.domain.ClientName
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.module.kotlin.jacksonMapperBuilder
import java.math.BigDecimal
import java.time.Instant

class ClientControllerTest {
    private val clientService = mockk<ClientService>()
    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(ClientController(clientService))
            .setControllerAdvice(ClientExceptionHandler())
            .setMessageConverters(JacksonJsonHttpMessageConverter(jacksonMapperBuilder()))
            .build()

    @Test
    fun `should get client`() {
        val client = client()
        every { clientService.getClient(client.id) } returns client

        mockMvc
            .perform(get("/api/clients/{clientId}", client.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(client.id))
            .andExpect(jsonPath("$.name.name").value(client.name.name))
            .andExpect(jsonPath("$.name.firstName").value(client.name.firstName))
            .andExpect(jsonPath("$.address.street").value(client.address.street))
            .andExpect(jsonPath("$.address.number").value(client.address.number))
            .andExpect(jsonPath("$.address.zipCode").value(client.address.zipCode))
            .andExpect(jsonPath("$.address.city").value(client.address.city))

        verify(exactly = 1) { clientService.getClient(client.id) }
    }

    @Test
    fun `should return 404 when client is not found`() {
        val clientId = "missing-client"
        every { clientService.getClient(clientId) } throws ClientNotFoundException(clientId)

        mockMvc
            .perform(get("/api/clients/{clientId}", clientId))
            .andExpect(status().isNotFound)

        verify(exactly = 1) { clientService.getClient(clientId) }
    }

    @Test
    fun `should get client accounts`() {
        val clientId = "client-id"
        val account = account(clientId)
        every { clientService.getClientAccounts(clientId) } returns listOf(account)

        mockMvc
            .perform(get("/api/clients/{clientId}/accounts", clientId))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].id").value(account.id))
            .andExpect(jsonPath("$[0].clientId").value(account.clientId))
            .andExpect(jsonPath("$[0].iban").value(account.iban))

        verify(exactly = 1) { clientService.getClientAccounts(clientId) }
    }

    @Test
    fun `should return 404 when client accounts are not found`() {
        val clientId = "missing-client"
        every { clientService.getClientAccounts(clientId) } throws ClientNotFoundException(clientId)

        mockMvc
            .perform(get("/api/clients/{clientId}/accounts", clientId))
            .andExpect(status().isNotFound)

        verify(exactly = 1) { clientService.getClientAccounts(clientId) }
    }

    @Test
    fun `should create client`() {
        val request = clientCreationRequest()
        val command = request.toCommand()
        val createdClient = client(id = "created-id")
        every { clientService.createClient(command) } returns createdClient

        mockMvc
            .perform(
                post("/api/clients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "name": {
                            "name": "${request.name.name}",
                            "firstName": "${request.name.firstName}"
                          },
                          "address": {
                            "street": "${request.address.street}",
                            "number": "${request.address.number}",
                            "zipCode": "${request.address.zipCode}",
                            "city": "${request.address.city}"
                          }
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(createdClient.id))
            .andExpect(jsonPath("$.name.name").value(createdClient.name.name))
            .andExpect(jsonPath("$.name.firstName").value(createdClient.name.firstName))
            .andExpect(jsonPath("$.address.street").value(createdClient.address.street))
            .andExpect(jsonPath("$.address.number").value(createdClient.address.number))
            .andExpect(jsonPath("$.address.zipCode").value(createdClient.address.zipCode))
            .andExpect(jsonPath("$.address.city").value(createdClient.address.city))

        verify(exactly = 1) { clientService.createClient(command) }
    }

    @Test
    fun `should update client`() {
        val request = clientUpdateRequest(id = "client-id")
        val command = request.toCommand()
        val updatedClient = client(id = request.id)
        every { clientService.updateClient(command) } returns updatedClient

        mockMvc
            .perform(
                put("/api/clients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "id": "${request.id}",
                          "name": {
                            "name": "${request.name?.name}",
                            "firstName": "${request.name?.firstName}"
                          },
                          "address": {
                            "street": "${request.address?.street}",
                            "number": "${request.address?.number}",
                            "zipCode": "${request.address?.zipCode}",
                            "city": "${request.address?.city}"
                          }
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(updatedClient.id))
            .andExpect(jsonPath("$.name.name").value(updatedClient.name.name))
            .andExpect(jsonPath("$.name.firstName").value(updatedClient.name.firstName))
            .andExpect(jsonPath("$.address.street").value(updatedClient.address.street))
            .andExpect(jsonPath("$.address.number").value(updatedClient.address.number))
            .andExpect(jsonPath("$.address.zipCode").value(updatedClient.address.zipCode))
            .andExpect(jsonPath("$.address.city").value(updatedClient.address.city))

        verify(exactly = 1) { clientService.updateClient(command) }
    }

    @Test
    fun `should delete client`() {
        val clientId = "client-id"
        every { clientService.deleteClient(clientId) } just runs

        mockMvc
            .perform(delete("/api/clients/{clientId}", clientId))
            .andExpect(status().isNoContent)

        verify(exactly = 1) { clientService.deleteClient(clientId) }
    }

    @Test
    fun `should return 404 when deleting missing client`() {
        val clientId = "missing-client"
        every { clientService.deleteClient(clientId) } throws ClientNotFoundException(clientId)

        mockMvc
            .perform(delete("/api/clients/{clientId}", clientId))
            .andExpect(status().isNotFound)

        verify(exactly = 1) { clientService.deleteClient(clientId) }
    }

    @Test
    fun `should return 404 when updating missing client`() {
        // Arrange
        val request = clientUpdateRequest(id = "missing-client")
        val command = request.toCommand()
        every { clientService.updateClient(command) } throws ClientNotFoundException(request.id)

        // Act
        mockMvc
            .perform(
                put("/api/clients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "id": "${request.id}",
                          "name": {
                            "name": "${request.name?.name}",
                            "firstName": "${request.name?.firstName}"
                          },
                          "address": {
                            "street": "${request.address?.street}",
                            "number": "${request.address?.number}",
                            "zipCode": "${request.address?.zipCode}",
                            "city": "${request.address?.city}"
                          }
                        }
                        """.trimIndent(),
                    ),
            ).andExpect(status().isNotFound)

        // Assert
        verify(exactly = 1) { clientService.updateClient(command) }
    }

    private fun client(id: String = "client-id") =
        Client(
            id = id,
            name = clientName(),
            address = clientAddress(),
        )

    private fun clientCreationRequest() =
        ClientCreationRequest(
            name = clientNameDto(),
            address = clientAddressDto(),
        )

    private fun clientUpdateRequest(id: String) =
        ClientUpdateRequest(
            id = id,
            name = clientNameDto(),
            address = clientAddressDto(),
        )

    private fun clientName() =
        ClientName(
            name = "Doe",
            firstName = "Jane",
        )

    private fun clientAddress() =
        ClientAddress(
            street = "Main Street",
            number = "1",
            zipCode = "12345",
            city = "Berlin",
        )

    private fun clientNameDto() =
        ClientNameDto(
            name = "Doe",
            firstName = "Jane",
        )

    private fun clientAddressDto() =
        ClientAddressDto(
            street = "Main Street",
            number = "1",
            zipCode = "12345",
            city = "Berlin",
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
