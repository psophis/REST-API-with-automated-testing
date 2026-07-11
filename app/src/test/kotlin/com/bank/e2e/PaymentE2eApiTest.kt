package com.bank.e2e

import com.bank.BankingBackendApplication
import com.bank.bankaccount.persistence.BankAccountEntity
import com.bank.bankaccount.persistence.BankAccountJpaRepository
import com.bank.client.persistence.ClientEntity
import com.bank.client.persistence.ClientJpaRepository
import com.bank.payment.domain.TransactionType
import com.bank.payment.persistence.TransactionJpaRepository
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.math.BigDecimal
import java.time.Instant

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [BankingBackendApplication::class],
)
@Testcontainers
class PaymentE2eApiTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var clientJpaRepository: ClientJpaRepository

    @Autowired
    private lateinit var bankAccountJpaRepository: BankAccountJpaRepository

    @Autowired
    private lateinit var transactionJpaRepository: TransactionJpaRepository

    @BeforeEach
    fun cleanDatabase() {
        transactionJpaRepository.deleteAll()
        bankAccountJpaRepository.deleteAll()
        clientJpaRepository.deleteAll()
    }

    @Test
    fun `transfers money into bank account`() {
        val client =
            clientJpaRepository.save(
                ClientEntity(
                    id = "client-id",
                    lastName = "Smith",
                    firstName = "Jane",
                    street = "Elm Street",
                    number = "456",
                    zipCode = "12345",
                    city = "Berlin",
                ),
            )
        val account =
            bankAccountJpaRepository.save(
                BankAccountEntity(
                    id = "account-id",
                    clientId = client.id,
                    iban = "DE12345678901234567890",
                    balance = BigDecimal("1000.00"),
                    createdAt = Instant.now(),
                ),
            )

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "amount": 100.00,
                    "recipientIban": "${account.iban}",
                    "senderIban": "DE1234567890123"
                }
                """.trimIndent(),
            ).`when`()
            .post("/api/payments/transfer")
            .then()
            .statusCode(200)

        val savedTransactions = transactionJpaRepository.findAllByAccountId(account.id)
        assertThat(savedTransactions).hasSize(1)

        val savedTransaction = savedTransactions.single()
        assertThat(savedTransaction.amount).isEqualTo(BigDecimal("100.00"))
        assertThat(savedTransaction.type).isEqualTo(TransactionType.TRANSFER)
        assertThat(savedTransaction.senderIban).isEqualTo("DE1234567890123")
        assertThat(savedTransaction.recipientIban).isEqualTo(account.iban)
        assertThat(bankAccountJpaRepository.findById(account.id).get().balance).isEqualTo(BigDecimal("1100.00"))
    }

    @Test
    fun `transfers money out of bank account`() {
        val client =
            clientJpaRepository.save(
                ClientEntity(
                    id = "client-id",
                    lastName = "Smith",
                    firstName = "Jane",
                    street = "Elm Street",
                    number = "456",
                    zipCode = "12345",
                    city = "Berlin",
                ),
            )
        val account =
            bankAccountJpaRepository.save(
                BankAccountEntity(
                    id = "account-id",
                    clientId = client.id,
                    iban = "DE12345678901234567890",
                    balance = BigDecimal("1000.00"),
                    createdAt = Instant.now(),
                ),
            )

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "amount": 100.00,
                    "recipientIban": "DE1234567890123",
                    "senderIban": "${account.iban}"
                }
                """.trimIndent(),
            ).`when`()
            .post("/api/payments/transfer")
            .then()
            .statusCode(200)

        val savedTransactions = transactionJpaRepository.findAllByAccountId(account.id)
        assertThat(savedTransactions).hasSize(1)

        val savedTransaction = savedTransactions.single()
        assertThat(savedTransaction.amount).isEqualTo(BigDecimal("100.00"))
        assertThat(savedTransaction.type).isEqualTo(TransactionType.TRANSFER)
        assertThat(savedTransaction.senderIban).isEqualTo(account.iban)
        assertThat(savedTransaction.recipientIban).isEqualTo("DE1234567890123")
        assertThat(bankAccountJpaRepository.findById(account.id).get().balance).isEqualTo(BigDecimal("900.00"))
    }

    @Test
    fun `transfer to non-existing bank account fails`() {
        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "amount": 100.00,
                    "recipientIban": "DE00000000000000000000",
                    "senderIban": "DE11111111111111111111"
                }
                """.trimIndent(),
            ).`when`()
            .post("/api/payments/transfer")
            .then()
            .statusCode(404)

        assertThat(transactionJpaRepository.findAll()).isEmpty()
    }

    @Test
    fun `deposits money`() {
        val client =
            clientJpaRepository.save(
                ClientEntity(
                    id = "client-id",
                    lastName = "Smith",
                    firstName = "Jane",
                    street = "Elm Street",
                    number = "456",
                    zipCode = "12345",
                    city = "Berlin",
                ),
            )
        val account =
            bankAccountJpaRepository.save(
                BankAccountEntity(
                    id = "account-id",
                    clientId = client.id,
                    iban = "DE12345678901234567890",
                    balance = BigDecimal("1000.00"),
                    createdAt = Instant.now(),
                ),
            )

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "bankAccountId": "${account.id}",
                    "amount": 100.00
                }
                """.trimIndent(),
            ).`when`()
            .post("/api/payments/deposit")
            .then()
            .statusCode(200)

        val savedTransactions = transactionJpaRepository.findAllByAccountId(account.id)
        assertThat(savedTransactions).hasSize(1)

        val savedTransaction = savedTransactions.single()
        assertThat(savedTransaction.amount).isEqualTo(BigDecimal("100.00"))
        assertThat(savedTransaction.type).isEqualTo(TransactionType.DEPOSIT)
        assertThat(savedTransaction.senderIban).isEqualTo(account.iban)
        assertThat(savedTransaction.recipientIban).isEqualTo(account.iban)
    }

    @Test
    fun `withdraws money`() {
        val client =
            clientJpaRepository.save(
                ClientEntity(
                    id = "client-id",
                    lastName = "Smith",
                    firstName = "Jane",
                    street = "Elm Street",
                    number = "456",
                    zipCode = "12345",
                    city = "Berlin",
                ),
            )
        val account =
            bankAccountJpaRepository.save(
                BankAccountEntity(
                    id = "account-id",
                    clientId = client.id,
                    iban = "DE12345678901234567890",
                    balance = BigDecimal("1000.00"),
                    createdAt = Instant.now(),
                ),
            )

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "bankAccountId": "${account.id}",
                    "amount": 100.00
                }
                """.trimIndent(),
            ).`when`()
            .post("/api/payments/withdrawal")
            .then()
            .statusCode(200)

        val savedTransactions = transactionJpaRepository.findAllByAccountId(account.id)
        assertThat(savedTransactions).hasSize(1)

        val savedTransaction = savedTransactions.single()
        assertThat(savedTransaction.amount).isEqualTo(BigDecimal("100.00"))
        assertThat(savedTransaction.type).isEqualTo(TransactionType.WITHDRAWAL)
        assertThat(savedTransaction.senderIban).isEqualTo(account.iban)
        assertThat(savedTransaction.recipientIban).isEqualTo(account.iban)
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
