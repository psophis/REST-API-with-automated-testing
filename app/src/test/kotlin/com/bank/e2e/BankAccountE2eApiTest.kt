package com.bank.e2e

import com.bank.BankingBackendApplication
import com.bank.bankaccount.persistence.BankAccountEntity
import com.bank.bankaccount.persistence.BankAccountJpaRepository
import com.bank.client.persistence.ClientEntity
import com.bank.client.persistence.ClientJpaRepository
import com.bank.payment.persistence.TransactionJpaRepository
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
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
class BankAccountE2eApiTest {
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
    fun `opens bank account`() {
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

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .body(
                """
                {
                    "clientId": "${client.id}"
                }
                """.trimIndent(),
            ).`when`()
            .post("/api/accounts")
            .then()
            .statusCode(201)
            .body("id", Matchers.notNullValue())
            .body("clientId", equalTo(client.id))
            .body("balance", equalTo(0.00))

        val savedAccounts = bankAccountJpaRepository.findAllByClientId(client.id)
        assertThat(savedAccounts).hasSize(1)
        assertThat(savedAccounts.single().balance).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun `closes bank account`() {
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
        val bankAccount =
            bankAccountJpaRepository.save(
                BankAccountEntity(
                    id = "bank-account-1",
                    clientId = client.id,
                    iban = "DE0123456789",
                    balance = BigDecimal.ZERO,
                    createdAt = Instant.now(),
                ),
            )

        given()
            .port(port)
            .contentType(ContentType.JSON)
            .`when`()
            .delete("/api/accounts/${bankAccount.id}")
            .then()
            .statusCode(204)

        assertThat(bankAccountJpaRepository.findAllByClientId(client.id)).hasSize(0)
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
