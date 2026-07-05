package com.bank.integration

import com.bank.BankingBackendApplication
import com.bank.bankaccount.application.BankAccountService
import com.bank.bankaccount.persistence.BankAccountEntity
import com.bank.bankaccount.persistence.BankAccountJpaRepository
import com.bank.client.domain.ClientRepository
import com.bank.client.persistence.ClientEntity
import com.bank.client.persistence.ClientJpaRepository
import com.bank.payment.domain.TransactionType
import com.bank.payment.persistence.TransactionEntity
import com.bank.payment.persistence.TransactionJpaRepository
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
class BankAccountTransactionIntegrationTest {
    @Autowired
    private lateinit var bankAccountJpaRepository: BankAccountJpaRepository

    @Autowired
    private lateinit var transactionJpaRepository: TransactionJpaRepository

    @Autowired
    private lateinit var clientJpaRepository: ClientJpaRepository

    @Autowired
    private lateinit var bankAccountService: BankAccountService

    @BeforeEach
    fun cleanDatabase() {
        bankAccountJpaRepository.deleteAll()
        transactionJpaRepository.deleteAll()
    }

    @Test
    fun `should delete transactions when deleting account`() {
        val client =
            ClientEntity(
                id = UUID.randomUUID().toString(),
                lastName = "Doe",
                firstName = "Jane",
                street = "Main Street",
                number = "456",
                city = "London",
                zipCode = "123456",
            )
        clientJpaRepository.save(client)

        val bankAccount =
            BankAccountEntity(
                id = UUID.randomUUID().toString(),
                clientId = client.id,
                iban = "DE12345678901234567890",
                balance = BigDecimal.ZERO,
                createdAt = Instant.now()
            )
        bankAccountJpaRepository.save(bankAccount)

        val transaction =
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                accountId = bankAccount.id,
                senderIban = "DE12345678901234567890",
                recipientIban = "DE09876543210987654321",
                amount = BigDecimal("100"),
                type = TransactionType.DEPOSIT,
                createdAt = Instant.now()
            )
        transactionJpaRepository.save(transaction)

        bankAccountService.deleteBankAccount(bankAccount.id)

        assertThat(bankAccountJpaRepository.findById(bankAccount.id)).isEmpty
        assertThat(transactionJpaRepository.findById(transaction.id)).isEmpty
        assertThat(clientJpaRepository.findById(client.id)).isNotEmpty
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
