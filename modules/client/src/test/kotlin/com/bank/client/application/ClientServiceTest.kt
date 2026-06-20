package com.bank.client.application

import com.bank.bankaccount.application.BankAccountService
import com.bank.bankaccount.domain.BankAccount
import com.bank.bankaccount.domain.BankAccountRepository
import com.bank.client.domain.Client
import com.bank.client.domain.ClientAddress
import com.bank.client.domain.ClientName
import com.bank.client.domain.ClientRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class ClientServiceTest {
    private lateinit var clientRepository: ClientRepository
    private lateinit var bankAccountRepository: BankAccountRepository
    private lateinit var bankAccountService: BankAccountService
    private lateinit var clientService: ClientService

    @BeforeEach
    fun setUp() {
        clientRepository = mockk()
        bankAccountRepository = mockk()
        bankAccountService = mockk()
        clientService = ClientService(clientRepository, bankAccountRepository, bankAccountService)
    }

    @Test
    fun `should get client`() {
        // Arrange
        val client = client()
        every { clientRepository.getClientById(client.id) } returns client

        // Act
        val result = clientService.getClient(client.id)

        // Assert
        assertThat(result).isEqualTo(client)
        verify(exactly = 1) { clientRepository.getClientById(client.id) }
    }

    @Test
    fun `should get client accounts`() {
        // Arrange
        val client = client()
        val accounts = listOf(account(client.id))
        every { clientRepository.getClientById(client.id) } returns client
        every { bankAccountRepository.getBankAccountsByClientId(client.id) } returns accounts

        // Act
        val result = clientService.getClientAccounts(client.id)

        // Assert
        assertThat(result).isEqualTo(accounts)
        verify(exactly = 1) { clientRepository.getClientById(client.id) }
        verify(exactly = 1) { bankAccountRepository.getBankAccountsByClientId(client.id) }
    }

    @Test
    fun `should create client`() {
        // Arrange
        val request = clientUpdateCommand(id = "request-id")
        val createdClientSlot = slot<Client>()
        val savedClient = client(id = "persisted-client-id")
        every { clientRepository.createClient(capture(createdClientSlot)) } returns savedClient
        every {
            bankAccountService.createBankAccount(
                clientId = savedClient.id,
            )
        } returns account(savedClient.id)

        // Act
        val result = clientService.createClient(request)

        // Assert
        assertThat(result).isEqualTo(savedClient)
        assertThat(createdClientSlot.captured.id).isNotEqualTo(request.id)
        assertThat(createdClientSlot.captured.name).isEqualTo(request.name)
        assertThat(createdClientSlot.captured.address).isEqualTo(request.address)
        verify(exactly = 1) { clientRepository.createClient(any()) }
        verify(exactly = 1) {
            bankAccountService.createBankAccount(
                clientId = savedClient.id,
            )
        }
    }

    @Test
    fun `should update client`() {
        // Arrange
        val request = clientUpdateCommand(id = "client-id")
        val updatedClient = client(id = request.id)
        every { clientRepository.updateClient(any()) } returns updatedClient

        // Act
        val result = clientService.updateClient(request)

        // Assert
        assertThat(result).isEqualTo(updatedClient)
        verify(exactly = 1) {
            clientRepository.updateClient(
                Client(
                    id = request.id,
                    name = request.name,
                    address = request.address,
                ),
            )
        }
    }

    @Test
    fun `should delete client`() {
        // Arrange
        val clientId = "client-id"
        every { clientRepository.deleteClientById(clientId) } just runs
        every { bankAccountRepository.deleteBankAccountByClientId(clientId) } just runs

        // Act
        clientService.deleteClient(clientId)

        // Assert
        verify(exactly = 1) { clientRepository.deleteClientById(clientId) }
        verify(exactly = 1) { bankAccountRepository.deleteBankAccountByClientId(clientId) }
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

    private fun clientUpdateCommand(id: String = "client-id") =
        ClientUpdateCommand(
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
