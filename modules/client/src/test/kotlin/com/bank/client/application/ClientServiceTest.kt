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
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
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
        val client = client("client-id")
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
        val client = client("client-id")
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
        val request = createClientCommand()
        val client = client(id = "persisted-client-id")
        every { clientRepository.createClient(any()) } returns client
        every {
            bankAccountService.createBankAccount(
                clientId = client.id,
            )
        } returns account(client.id)

        // Act
        val result = clientService.createClient(request)

        // Assert
        assertThat(result).isEqualTo(client)
        assertThat(result.id).isEqualTo(client.id)
        assertThat(result.name).isEqualTo(client.name)
        assertThat(result.address).isEqualTo(client.address)
        verify(exactly = 1) { clientRepository.createClient(any()) }
        verify(exactly = 1) {
            bankAccountService.createBankAccount(
                clientId = client.id,
            )
        }
    }

    @Test
    fun `should update client`() {
        // Arrange
        val request = updateClientCommand(id = "client-id")
        val existingClient = client(id = request.id)
        val expectedClient =
            existingClient.copy(
                name = request.name ?: existingClient.name,
                address = request.address ?: existingClient.address,
            )

        every { clientRepository.getClientById(request.id) } returns existingClient
        every { clientRepository.updateClient(expectedClient) } returns expectedClient

        // Act
        val result = clientService.updateClient(request)

        // Assert
        assertThat(result).isEqualTo(expectedClient)
        verify(exactly = 1) { clientRepository.getClientById(request.id) }
        verify(exactly = 1) { clientRepository.updateClient(expectedClient) }
    }

    @Test
    fun `should delete client`() {
        // Arrange
        val client = client("client-id")
        every { clientRepository.getClientById(client.id) } returns client
        every { clientRepository.deleteClientById(client.id) } just runs
        every { bankAccountService.deleteBankAccount(any()) } just runs
        every { bankAccountRepository.getBankAccountsByClientId(client.id) } returns
            listOf(account(client.id).copy(balance = BigDecimal.ZERO))

        // Act
        clientService.deleteClient(client.id)

        // Assert
        verify(exactly = 2) { clientRepository.getClientById(client.id) }
        verify(exactly = 1) { clientRepository.deleteClientById(client.id) }
        verify(exactly = 1) { bankAccountService.deleteBankAccount(any()) }
        verify(exactly = 1) { bankAccountRepository.getBankAccountsByClientId(client.id) }
    }

    @Test
    fun `should throw ClientNotFoundException when client does not exist`() {
        // Arrange
        val clientId = "non-existing-client-id"
        every { clientRepository.getClientById(clientId) } returns null

        // Act & Assert
        val result =
            assertThrows(ClientNotFoundException::class.java) {
                clientService.deleteClient(clientId)
            }

        assertThat(result.message).isEqualTo("Could not find client with id $clientId")
        verify(exactly = 1) { clientRepository.getClientById(clientId) }
    }

    @Test
    fun `should return empty account list when client has no accounts`() {
        // Arrange
        val client = client("client-id")
        every { clientRepository.getClientById(client.id) } returns client
        every { bankAccountRepository.getBankAccountsByClientId(client.id) } returns emptyList()

        // Act
        val result = clientService.getClientAccounts(client.id)

        assertThat(result).isEmpty()
        verify { clientRepository.getClientById(client.id) }
        verify { bankAccountRepository.getBankAccountsByClientId(client.id) }
    }

    @Test
    fun `should throw ClientNotFoundException when updating non-existing client`() {
        // Arrange
        val request = updateClientCommand(id = "non-existing-client-id")
        every { clientRepository.getClientById(request.id) } returns null

        // Act & Assert
        val result =
            assertThrows(ClientNotFoundException::class.java) {
                clientService.updateClient(request)
            }

        assertThat(result.message).isEqualTo("Could not find client with id ${request.id}")
        verify(exactly = 1) { clientRepository.getClientById(request.id) }
        verify(exactly = 0) { clientRepository.updateClient(any()) }
    }

    @Test
    fun `should throw ClientNotFoundException when deleting missing client`() {
        // Arrange
        val clientId = "non-existing-client-id"
        every { clientRepository.getClientById(clientId) } returns null

        // Act & Assert
        val result =
            assertThrows(ClientNotFoundException::class.java) {
                clientService.deleteClient(clientId)
            }

        assertThat(result.message).isEqualTo("Could not find client with id $clientId")
        verify(exactly = 1) { clientRepository.getClientById(clientId) }
        verify(exactly = 0) { clientRepository.deleteClientById(any()) }
        verify(exactly = 0) { bankAccountRepository.deleteBankAccountsByClientId(any()) }
    }

    @Test
    fun `should throw ClientHasNonZeroBalanceException when deleting client with non-zero balance`() {
        // Arrange
        val client = client("client-id")
        every { clientRepository.getClientById(client.id) } returns client
        every { bankAccountRepository.getBankAccountsByClientId(client.id) } returns
            listOf(account(client.id).copy(balance = BigDecimal("100")))

        // Act & Assert
        val result =
            assertThrows(ClientHasNonZeroBalanceException::class.java) {
                clientService.deleteClient(client.id)
            }
        assertThat(result.message).isEqualTo(
            "Could not delete client with id ${client.id} because at least one bank account has a non-zero balance",
        )

        verify(exactly = 1) { clientRepository.getClientById(client.id) }
        verify(exactly = 1) { bankAccountRepository.getBankAccountsByClientId(client.id) }
        verify(exactly = 0) { bankAccountRepository.deleteBankAccountsByClientId(any()) }
        verify(exactly = 0) { clientRepository.deleteClientById(any()) }
    }

    private fun client(id: String) =
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

    private fun createClientCommand() =
        CreateClientCommand(
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

    private fun updateClientCommand(id: String) =
        UpdateClientCommand(
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
