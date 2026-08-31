package com.bank.client.persistence

import com.bank.client.domain.Client
import com.bank.client.domain.ClientAddress
import com.bank.client.domain.ClientName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.util.UUID
import kotlin.test.Test

@DataJpaTest(
    properties = [
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.flyway.enabled=true",
    ],
)
@Testcontainers
@Import(ClientRepositoryImpl::class)
@ContextConfiguration(classes = [ClientRepositoryIntegrationTest.JpaTestConfig::class])
class ClientRepositoryIntegrationTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("com.bank.client.persistence")
    @EnableJpaRepositories("com.bank.client.persistence")
    class JpaTestConfig

    @Autowired
    private lateinit var clientRepository: ClientRepositoryImpl

    @Autowired
    private lateinit var clientJpaRepository: ClientJpaRepository

    @Test
    fun `should load client by id`() {
        val client = client()
        clientRepository.createClient(client)

        val loaded = clientRepository.getClientById(client.id)

        assertThat(loaded).isEqualTo(client)
    }

    @Test
    fun `should delete client by id`() {
        val client = client()
        clientRepository.createClient(client)

        clientRepository.deleteClientById(client.id)

        assertThat(clientRepository.getClientById(client.id)).isNull()
        assertThat(clientJpaRepository.findById(client.id)).isEmpty
    }

    @Test
    fun `should throw when client does not exist`() {
        val clientId = "missing-client"

        assertThrows(NoSuchElementException::class.java) {
            clientRepository.deleteClientById(clientId)
        }
    }

    @Test
    fun `should update client`() {
        val client = client()
        clientRepository.createClient(client)
        val updatedClient = client.copy(name = ClientName("Smith", "Jane"))

        val result = clientRepository.updateClient(updatedClient)

        assertThat(result).isEqualTo(updatedClient)
    }

    private fun client(
        id: String = UUID.randomUUID().toString(),
        name: ClientName = ClientName("Doe", "John"),
        address: ClientAddress = ClientAddress("Main Street", "123", "12345", "Berlin"),
    ) = Client(
        id = id,
        name = name,
        address = address,
    )

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer("postgres:17-alpine")

        @JvmStatic
        @DynamicPropertySource
        fun postgresProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
