package com.bank.bankaccount.integration

import com.bank.bankaccount.domain.BankAccount
import com.bank.bankaccount.persistence.BankAccountEntity
import com.bank.bankaccount.persistence.BankAccountJpaRepository
import com.bank.bankaccount.persistence.BankAccountRepositoryImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ContextConfiguration
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@DataJpaTest
@Import(BankAccountRepositoryImpl::class)
@ContextConfiguration(classes = [BankAccountRepositoryIntegrationTest.JpaTestConfig::class])
class BankAccountRepositoryIntegrationTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("com.bank.bankaccount.persistence")
    @EnableJpaRepositories("com.bank.bankaccount.persistence")
    class JpaTestConfig

    @Autowired
    lateinit var bankAccountRepository: BankAccountRepositoryImpl

    @Autowired
    lateinit var bankAccountJpaRepository: BankAccountJpaRepository

    @Test
    fun `should create and load account`() {
        // Arrange
        val bankAccountId = UUID.randomUUID().toString()
        val iban = "DE${UUID.randomUUID().toString().take(10)}"
        val bankAccount =
            BankAccount(
                id = bankAccountId,
                clientId = "client-1",
                iban = iban,
                balance = BigDecimal.ZERO,
                createdAt = Instant.now(),
            )
        bankAccountRepository.createBankAccount(bankAccount)

        // Act
        val loaded = bankAccountRepository.getBankAccountById(bankAccountId)

        // Assert
        assertThat(loaded).isNotNull
        assertEquals(bankAccount.id, loaded?.id)
        assertEquals(bankAccount.clientId, loaded?.clientId)
        assertEquals(bankAccount.iban, loaded?.iban)
    }

    @Test
    fun `should find account by iban`() {
        // Arrange
        val bankAccountId = UUID.randomUUID().toString()
        val iban = "DE${UUID.randomUUID().toString().take(10)}"
        val entity =
            BankAccountEntity(
                id = bankAccountId,
                clientId = "client-1",
                iban = iban,
                balance = BigDecimal.ZERO,
                createdAt = Instant.now(),
            )

        bankAccountJpaRepository.save(entity)

        // Act
        val result = bankAccountRepository.getBankAccountByIban(iban)

        // Assert
        assertThat(result).isNotNull
        assertEquals(bankAccountId, result?.id)
    }

    @Test
    fun `should return null when iban not found`() {
        // Act
        val result = bankAccountRepository.getBankAccountByIban("UNKNOWN")

        // Assert
        assertNull(result)
    }

    @Test
    fun `should increase account balance`() {
        // Arrange
        val bankAccountId = UUID.randomUUID().toString()
        val iban = "DE${UUID.randomUUID().toString().take(10)}"
        val entity =
            BankAccountEntity(
                id = bankAccountId,
                clientId = "client-1",
                iban = iban,
                balance = BigDecimal("100"),
                createdAt = Instant.now(),
            )

        bankAccountJpaRepository.save(entity)

        // Act
        bankAccountRepository.increaseBankAccountBalance(
            bankAccountId = bankAccountId,
            amount = BigDecimal("50"),
        )

        // Assert
        val updated = bankAccountJpaRepository.findById(bankAccountId).get()
        assertEquals(BigDecimal("150"), updated.balance)
    }

    @Test
    fun `should reduce account balance`() {
        // Arrange
        val bankAccountId = UUID.randomUUID().toString()
        val iban = "DE${UUID.randomUUID().toString().take(10)}"
        val entity =
            BankAccountEntity(
                id = bankAccountId,
                clientId = "client-1",
                iban = iban,
                balance = BigDecimal("100"),
                createdAt = Instant.now(),
            )

        bankAccountJpaRepository.save(entity)

        // Act
        bankAccountRepository.decreaseBankAccountBalance(
            bankAccountId = bankAccountId,
            amount = BigDecimal("40"),
        )

        // Assert
        val updated = bankAccountJpaRepository.findById(bankAccountId).get()
        assertEquals(BigDecimal("60"), updated.balance)
    }

    @Test
    fun `should delete account`() {
        // Arrange
        val bankAccountId = UUID.randomUUID().toString()
        val iban = "DE${UUID.randomUUID().toString().take(10)}"
        val entity =
            BankAccountEntity(
                id = bankAccountId,
                clientId = "client-1",
                iban = iban,
                balance = BigDecimal.ZERO,
                createdAt = Instant.now(),
            )

        bankAccountJpaRepository.save(entity)

        // Act
        bankAccountRepository.deleteByBankAccountId(bankAccountId)

        val deleted = bankAccountJpaRepository.findById(bankAccountId)

        // Assert
        assertTrue(deleted.isEmpty)
    }

    @Test
    fun `should throw exception for negative increase amount`() {
        val bankAccountId = UUID.randomUUID().toString()

        assertThrows(IllegalArgumentException::class.java) {
            bankAccountRepository.increaseBankAccountBalance(
                bankAccountId = bankAccountId,
                amount = BigDecimal("-1"),
            )
        }
    }

    @Test
    fun `should return null when account id is not found`() {
        val result = bankAccountRepository.getBankAccountById("non-existent-id")

        assertNull(result)
    }

    @Test
    fun `should find accounts by client id`() {
        // Arrange
        val clientId = "client-1"
        val firstAccount =
            BankAccountEntity(
                id = UUID.randomUUID().toString(),
                clientId = clientId,
                iban = "DE${UUID.randomUUID().toString().take(10)}",
                balance = BigDecimal.ZERO,
                createdAt = Instant.now(),
            )
        val secondAccount =
            BankAccountEntity(
                id = UUID.randomUUID().toString(),
                clientId = clientId,
                iban = "DE${UUID.randomUUID().toString().take(10)}",
                balance = BigDecimal("100"),
                createdAt = Instant.now(),
            )

        bankAccountJpaRepository.save(firstAccount)
        bankAccountJpaRepository.save(secondAccount)

        // Act
        val result = bankAccountRepository.getBankAccountsByClientId(clientId)

        // Assert
        assertEquals(2, result.size)
        assertTrue(result.any { it.id == firstAccount.id })
        assertTrue(result.any { it.id == secondAccount.id })
    }

    @Test
    fun `should return empty list when client has no accounts`() {
        // Act
        val result = bankAccountRepository.getBankAccountsByClientId("unknown-client")

        // Assert
        assertTrue(result.isEmpty())
    }

    @Test
    fun `should throw exception when increasing balance for unknown account`() {
        assertThrows(NoSuchElementException::class.java) {
            bankAccountRepository.increaseBankAccountBalance(
                bankAccountId = "unknown-account",
                amount = BigDecimal("10"),
            )
        }
    }

    @Test
    fun `should throw exception for zero increase amount`() {
        assertThrows(IllegalArgumentException::class.java) {
            bankAccountRepository.increaseBankAccountBalance(
                bankAccountId = UUID.randomUUID().toString(),
                amount = BigDecimal.ZERO,
            )
        }
    }

    @Test
    fun `should throw exception for negative decrease amount`() {
        assertThrows(IllegalArgumentException::class.java) {
            bankAccountRepository.decreaseBankAccountBalance(
                bankAccountId = UUID.randomUUID().toString(),
                amount = BigDecimal("-1"),
            )
        }
    }

    @Test
    fun `should throw exception for zero decrease amount`() {
        assertThrows(IllegalArgumentException::class.java) {
            bankAccountRepository.decreaseBankAccountBalance(
                bankAccountId = UUID.randomUUID().toString(),
                amount = BigDecimal.ZERO,
            )
        }
    }

    @Test
    fun `should throw exception when decreasing balance for unknown account`() {
        assertThrows(NoSuchElementException::class.java) {
            bankAccountRepository.decreaseBankAccountBalance(
                bankAccountId = "unknown-account",
                amount = BigDecimal("10"),
            )
        }
    }

    @Test
    fun `should throw exception when decreasing more than balance`() {
        // Arrange
        val bankAccountId = UUID.randomUUID().toString()
        val entity =
            BankAccountEntity(
                id = bankAccountId,
                clientId = "client-1",
                iban = "DE${UUID.randomUUID().toString().take(10)}",
                balance = BigDecimal("100"),
                createdAt = Instant.now(),
            )

        bankAccountJpaRepository.save(entity)

        // Act/Assert
        assertThrows(IllegalStateException::class.java) {
            bankAccountRepository.decreaseBankAccountBalance(
                bankAccountId = bankAccountId,
                amount = BigDecimal("150"),
            )
        }
    }

    @Test
    fun `should delete accounts by client id`() {
        // Arrange
        val clientId = "client-1"
        val firstAccount =
            BankAccountEntity(
                id = UUID.randomUUID().toString(),
                clientId = clientId,
                iban = "DE${UUID.randomUUID().toString().take(10)}",
                balance = BigDecimal.ZERO,
                createdAt = Instant.now(),
            )
        val secondAccount =
            BankAccountEntity(
                id = UUID.randomUUID().toString(),
                clientId = clientId,
                iban = "DE${UUID.randomUUID().toString().take(10)}",
                balance = BigDecimal("100"),
                createdAt = Instant.now(),
            )
        val otherClientAccount =
            BankAccountEntity(
                id = UUID.randomUUID().toString(),
                clientId = "client-2",
                iban = "DE${UUID.randomUUID().toString().take(10)}",
                balance = BigDecimal("200"),
                createdAt = Instant.now(),
            )

        bankAccountJpaRepository.save(firstAccount)
        bankAccountJpaRepository.save(secondAccount)
        bankAccountJpaRepository.save(otherClientAccount)

        // Act
        bankAccountRepository.deleteBankAccountsByClientId(clientId)

        // Assert
        assertTrue(bankAccountJpaRepository.findById(firstAccount.id).isEmpty)
        assertTrue(bankAccountJpaRepository.findById(secondAccount.id).isEmpty)
        assertTrue(bankAccountJpaRepository.findById(otherClientAccount.id).isPresent)
    }

    @Test
    fun `should do nothing when deleting accounts by unknown client id`() {
        // Arrange
        val entity =
            BankAccountEntity(
                id = UUID.randomUUID().toString(),
                clientId = "client-1",
                iban = "DE${UUID.randomUUID().toString().take(10)}",
                balance = BigDecimal.ZERO,
                createdAt = Instant.now(),
            )

        bankAccountJpaRepository.save(entity)

        // Act
        bankAccountRepository.deleteBankAccountsByClientId("unknown-client")

        // Assert
        assertTrue(bankAccountJpaRepository.findById(entity.id).isPresent)
    }
}
