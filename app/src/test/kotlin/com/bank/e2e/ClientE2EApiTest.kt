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
    classes = [BankingBackendApplication::class]
)
@Testcontainers
class ClientE2EApiTest {
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
    fun `creates client account with bank account`() {
        val clientId =
            given()
                .port(port)
                .contentType(ContentType.JSON)
                .body(
                    """
                    {
                        "name": {
                            "name": "Doe",
                            "firstName": "Jane"
                        },
                        "address": {
                            "street": "Main Street",
                            "number": "123",
                            "city": "Berlin",
                            "zipCode": "10001"
                        }
                    }
                    """.trimIndent()
                )
            .`when`()
                .post("/api/clients")
            .then()
                .statusCode(201)
                .body("id", Matchers.notNullValue())
                .body("name.name", equalTo("Doe"))
                .body("name.firstName", equalTo("Jane"))
                .body("address.street", equalTo("Main Street"))
                .body("address.number", equalTo("123"))
                .body("address.city", equalTo("Berlin"))
                .body("address.zipCode", equalTo("10001"))
                .extract()
                .path<String>("id")

        val savedClient = clientJpaRepository.findById(clientId)
        assertThat(savedClient).isPresent
        assertThat(savedClient.get().id).isEqualTo(clientId)
        assertThat(savedClient.get().lastName).isEqualTo("Doe")
        assertThat(savedClient.get().firstName).isEqualTo("Jane")
        assertThat(savedClient.get().street).isEqualTo("Main Street")
        assertThat(savedClient.get().number).isEqualTo("123")
        assertThat(savedClient.get().city).isEqualTo("Berlin")
        assertThat(savedClient.get().zipCode).isEqualTo("10001")

        val bankAccounts = bankAccountJpaRepository.findAllByClientId(clientId)
        assertThat(bankAccounts).hasSize(1)
        assertThat(bankAccounts.single().clientId).isEqualTo(clientId)
        assertThat(bankAccounts.single().balance).isEqualTo(BigDecimal.ZERO)
    }

    @Test
    fun `deletes bank account and transactions when deleting client`() {
        val client =
            clientJpaRepository.save(
                ClientEntity(
                    id = "client-1",
                    lastName = "Doe",
                    firstName = "Jane",
                    street = "Main Street",
                    number = "123",
                    city = "Berlin",
                    zipCode = "10001"
                )
            )
        val bankAccount =
            bankAccountJpaRepository.save(
                BankAccountEntity(
                    id = "bank-account-1",
                    clientId = client.id,
                    iban = "DE0123456789",
                    balance = BigDecimal.ZERO,
                    createdAt = Instant.now()
                )
            )
        val transaction1 =
            TransactionEntity(
                id = "transaction-1",
                accountId = bankAccount.id,
                senderIban = "DE0123456789",
                recipientIban = "DE9876543210",
                amount = BigDecimal("500"),
                type = TransactionType.WITHDRAWAL,
                createdAt = Instant.now()
            )
        val transaction2 =
            TransactionEntity(
                id = "transaction-2",
                accountId = bankAccount.id,
                senderIban = "DE0123456789",
                recipientIban = "DE9876543210",
                amount = BigDecimal("1000"),
                type = TransactionType.DEPOSIT,
                createdAt = Instant.now()
            )
        transactionJpaRepository.saveAll(listOf(transaction1, transaction2))

        given()
            .port(port)
            .contentType(ContentType.JSON)
        .`when`()
            .delete("/api/clients/${client.id}")
        .then()
            .statusCode(204)

        assertThat(clientJpaRepository.findById(client.id)).isEmpty
        assertThat(bankAccountJpaRepository.findById(bankAccount.id)).isEmpty
        assertThat(transactionJpaRepository.findById(transaction1.id)).isEmpty
        assertThat(transactionJpaRepository.findById(transaction2.id)).isEmpty
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