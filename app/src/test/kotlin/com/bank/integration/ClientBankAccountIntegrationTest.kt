package com.bank.integration

import com.bank.BankingBackendApplication
import com.bank.bankaccount.persistence.BankAccountEntity
import com.bank.bankaccount.persistence.BankAccountJpaRepository
import com.bank.client.application.ClientService
import com.bank.client.application.CreateClientCommand
import com.bank.client.domain.ClientAddress
import com.bank.client.domain.ClientName
import com.bank.client.persistence.ClientEntity
import com.bank.client.persistence.ClientJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@SpringBootTest(classes = [BankingBackendApplication::class])
@Testcontainers
class ClientBankAccountIntegrationTest{
    @Autowired
    private lateinit var bankAccountJpaRepository: BankAccountJpaRepository

    @Autowired
    private lateinit var clientService: ClientService

    @Autowired
    private lateinit var clientJpaRepository: ClientJpaRepository

    @BeforeEach
    fun cleanDatabase() {
        bankAccountJpaRepository.deleteAll()
        clientJpaRepository.deleteAll()
    }

    @Test
    fun `should create bank account when creating new client`() {
        val clientCommand =
            CreateClientCommand(
                name =
                    ClientName(
                        firstName = "John",
                        name = "Doe",
                    ),
                address =
                    ClientAddress(
                        street = "Street",
                        number = "456",
                        zipCode = "1234",
                        city = "London"
                    )
            )

        val createdClient = clientService.createClient(clientCommand)

        val bankAccounts = bankAccountJpaRepository.findAllByClientId(createdClient.id)

        assertThat(bankAccounts).hasSize(1)
        assertThat(bankAccounts.single().clientId).isEqualTo(createdClient.id)
        assertThat(bankAccounts.single().iban).isNotBlank()
        assertThat(bankAccounts.single().balance).isEqualByComparingTo(BigDecimal.ZERO)
        assertThat(bankAccounts.single().createdAt).isNotNull()
    }

    @Test
    fun `should delete bank account when deleting client`() {
        val client =
            ClientEntity(
                id = UUID.randomUUID().toString(),
                lastName = "Doe",
                firstName = "John",
                street = "Street",
                number = "456",
                zipCode = "1234",
                city = "London"
            )
        clientJpaRepository.save(client)

        val bankAccount =
            BankAccountEntity(
                id = UUID.randomUUID().toString(),
                clientId = client.id,
                iban = "123456789",
                balance = BigDecimal("1000"),
                createdAt = Instant.now(),
            )
        bankAccountJpaRepository.save(bankAccount)

        clientService.deleteClient(client.id)

        assertThat(clientJpaRepository.findById(client.id)).isEmpty
        assertThat(bankAccountJpaRepository.findAllByClientId(client.id)).isEmpty()
        assertThat(bankAccountJpaRepository.findById(bankAccount.id)).isEmpty
    }

    companion object {
        @Container
        private val postgres = PostgreSQLContainer("postgres:17-alpine")

        @JvmStatic
        @DynamicPropertySource
        fun postgresProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
