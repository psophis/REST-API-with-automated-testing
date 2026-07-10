package com.bank.e2e

import com.bank.BankingBackendApplication
import com.bank.bankaccount.persistence.BankAccountEntity
import com.bank.bankaccount.persistence.BankAccountJpaRepository
import com.bank.client.persistence.ClientEntity
import com.bank.client.persistence.ClientJpaRepository
import com.bank.payment.domain.TransactionType
import com.bank.payment.persistence.TransactionEntity
import com.bank.payment.persistence.TransactionJpaRepository
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.equalTo
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
class TransactionE2EApiTest {
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
        bankAccountJpaRepository.deleteAll()
        transactionJpaRepository.deleteAll()
        clientJpaRepository.deleteAll()
    }

    @Test
    fun `loads transaction history`() {
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
        val transaction1 =
            TransactionEntity(
                id = "transaction-id-1",
                accountId = account.id,
                senderIban = account.iban,
                recipientIban = "DE09876543210987654321",
                amount = BigDecimal("100.00"),
                type = TransactionType.TRANSFER,
                createdAt = Instant.now(),
            )
        val transaction2 =
            TransactionEntity(
                id = "transaction-id-3",
                accountId = account.id,
                senderIban = "DE09876543210987654321",
                recipientIban = account.iban,
                amount = BigDecimal("100.00"),
                type = TransactionType.TRANSFER,
                createdAt = Instant.now(),
            )
        val transaction3 =
            TransactionEntity(
                id = "transaction-id-2",
                accountId = account.id,
                senderIban = account.iban,
                recipientIban = account.iban,
                amount = BigDecimal("100.00"),
                type = TransactionType.WITHDRAWAL,
                createdAt = Instant.now(),
            )
        val transaction4 =
            TransactionEntity(
                id = "transaction-id-4",
                accountId = account.id,
                senderIban = account.iban,
                recipientIban = account.iban,
                amount = BigDecimal("500.00"),
                type = TransactionType.DEPOSIT,
                createdAt = Instant.now(),
            )
        transactionJpaRepository.saveAll(listOf(transaction1, transaction2, transaction3, transaction4))

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .`when`()
            .get("/api/transactions/${account.id}/transactions")
            .then()
            .statusCode(200)
            .body("size()", equalTo(4))
            .body("[0].id", equalTo(transaction1.id))
            .body("[1].id", equalTo(transaction2.id))
            .body("[2].id", equalTo(transaction3.id))
            .body("[3].id", equalTo(transaction4.id))
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
            .`when`()
            .get("/api/transactions/${account.id}/transactions")
            .then()
            .statusCode(200)
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
