package com.bank.integration

import com.bank.BankingBackendApplication
import com.bank.bankaccount.persistence.BankAccountEntity
import com.bank.bankaccount.persistence.BankAccountJpaRepository
import com.bank.client.persistence.ClientEntity
import com.bank.client.persistence.ClientJpaRepository
import com.bank.payment.api.BankTransferRequest
import com.bank.payment.api.DepositRequest
import com.bank.payment.api.PaymentController
import com.bank.payment.api.WithdrawalRequest
import com.bank.payment.domain.TransactionType
import com.bank.payment.persistence.TransactionJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
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
class PaymentIntegrationTest {
    @Autowired
    private lateinit var paymentController: PaymentController

    @Autowired
    private lateinit var bankAccountJpaRepository: BankAccountJpaRepository

    @Autowired
    private lateinit var transactionJpaRepository: TransactionJpaRepository

    @Autowired
    private lateinit var clientJpaRepository: ClientJpaRepository

    @BeforeEach
    fun cleanDatabase() {
        transactionJpaRepository.deleteAll()
        bankAccountJpaRepository.deleteAll()
        clientJpaRepository.deleteAll()
    }

    @Test
    fun `should create transaction and update balance when depositing money`() {
        // Arrange
        val client =
            clientJpaRepository.save(
                ClientEntity(
                    id = UUID.randomUUID().toString(),
                    lastName = "Smith",
                    firstName = "Jane",
                    street = "Main Street",
                    number = "45",
                    city = "Berlin",
                    zipCode = "12345",
                ),
            )
        val account =
            bankAccountJpaRepository.save(
                BankAccountEntity(
                    id = UUID.randomUUID().toString(),
                    clientId = client.id,
                    iban = "DE12345678901234567890",
                    balance = BigDecimal.ZERO,
                    createdAt = Instant.now(),
                ),
            )
        val depositRequest =
            DepositRequest(
                bankAccountId = account.id,
                amount = BigDecimal("100.00"),
            )

        // Act
        val response = paymentController.depositMoney(depositRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val updatedAccount = bankAccountJpaRepository.findById(account.id).orElseThrow()
        assertThat(updatedAccount.balance).isEqualTo(BigDecimal("100.00"))

        val transactions = transactionJpaRepository.findAllByAccountId(account.id)
        assertThat(transactions).hasSize(1)
        assertThat(transactions.single().amount).isEqualByComparingTo("100.00")
        assertThat(transactions.single().type).isEqualTo(TransactionType.DEPOSIT)
        assertThat(transactions.single().senderIban).isEqualTo(account.iban)
        assertThat(transactions.single().recipientIban).isEqualTo(account.iban)
    }

    @Test
    fun `should create transaction and update balance when withdrawing money`() {
        // Arrange
        val client =
            clientJpaRepository.save(
                ClientEntity(
                    id = UUID.randomUUID().toString(),
                    lastName = "Smith",
                    firstName = "Jane",
                    street = "Main Street",
                    number = "45",
                    city = "Berlin",
                    zipCode = "12345",
                ),
            )
        val account =
            bankAccountJpaRepository.save(
                BankAccountEntity(
                    id = UUID.randomUUID().toString(),
                    clientId = client.id,
                    iban = "DE12345678901234567890",
                    balance = BigDecimal("100.00"),
                    createdAt = Instant.now(),
                ),
            )
        val withdrawalRequest =
            WithdrawalRequest(
                bankAccountId = account.id,
                amount = BigDecimal("50.00"),
            )

        // Act
        val response = paymentController.withdrawMoney(withdrawalRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val updatedAccount = bankAccountJpaRepository.findById(account.id).orElseThrow()
        assertThat(updatedAccount.balance).isEqualTo(BigDecimal("50.00"))

        val transactions = transactionJpaRepository.findAllByAccountId(account.id)
        assertThat(transactions).hasSize(1)
        assertThat(transactions.single().amount).isEqualByComparingTo("50.00")
        assertThat(transactions.single().type).isEqualTo(TransactionType.WITHDRAWAL)
        assertThat(transactions.single().senderIban).isEqualTo(account.iban)
        assertThat(transactions.single().recipientIban).isEqualTo(account.iban)
    }

    @Test
    fun `should create transaction and update balance when sending bank transfer`() {
        // Arrange
        val client =
            clientJpaRepository.save(
                ClientEntity(
                    id = UUID.randomUUID().toString(),
                    lastName = "Smith",
                    firstName = "Jane",
                    street = "Main Street",
                    number = "45",
                    city = "Berlin",
                    zipCode = "12345",
                ),
            )
        val account =
            bankAccountJpaRepository.save(
                BankAccountEntity(
                    id = UUID.randomUUID().toString(),
                    clientId = client.id,
                    iban = "DE12345678901234567890",
                    balance = BigDecimal("100.00"),
                    createdAt = Instant.now(),
                ),
            )
        val bankTransferRequest =
            BankTransferRequest(
                amount = BigDecimal("50.00"),
                recipientIban = "DE0987654321",
                senderIban = account.iban,
            )

        // Act
        val response = paymentController.sendBankTransfer(bankTransferRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val updatedAccount = bankAccountJpaRepository.findById(account.id).orElseThrow()
        assertThat(updatedAccount.balance).isEqualTo(BigDecimal("50.00"))

        val transactions = transactionJpaRepository.findAllByAccountId(account.id)
        assertThat(transactions).hasSize(1)
        assertThat(transactions.single().amount).isEqualByComparingTo("50.00")
        assertThat(transactions.single().type).isEqualTo(TransactionType.TRANSFER)
        assertThat(transactions.single().senderIban).isEqualTo(account.iban)
        assertThat(transactions.single().recipientIban).isEqualTo("DE0987654321")
    }

    @Test
    fun `should create transaction and update balance when receiving bank transfer`() {
        // Arrange
        val client =
            clientJpaRepository.save(
                ClientEntity(
                    id = UUID.randomUUID().toString(),
                    lastName = "Smith",
                    firstName = "Jane",
                    street = "Main Street",
                    number = "45",
                    city = "Berlin",
                    zipCode = "12345",
                ),
            )
        val account =
            bankAccountJpaRepository.save(
                BankAccountEntity(
                    id = UUID.randomUUID().toString(),
                    clientId = client.id,
                    iban = "DE12345678901234567890",
                    balance = BigDecimal("100.00"),
                    createdAt = Instant.now(),
                ),
            )
        val bankTransferRequest =
            BankTransferRequest(
                amount = BigDecimal("50.00"),
                recipientIban = account.iban,
                senderIban = "DE0987654321",
            )

        // Act
        val response = paymentController.sendBankTransfer(bankTransferRequest)
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)

        val updatedAccount = bankAccountJpaRepository.findById(account.id).orElseThrow()
        assertThat(updatedAccount.balance).isEqualTo(BigDecimal("150.00"))

        val transactions = transactionJpaRepository.findAllByAccountId(account.id)
        assertThat(transactions).hasSize(1)
        assertThat(transactions.single().amount).isEqualByComparingTo("50.00")
        assertThat(transactions.single().type).isEqualTo(TransactionType.TRANSFER)
        assertThat(transactions.single().recipientIban).isEqualTo(account.iban)
        assertThat(transactions.single().senderIban).isEqualTo("DE0987654321")
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
