package com.bank.bankaccount.persistence

import com.bank.bankaccount.domain.BankAccount
import org.assertj.core.api.Assertions.assertThat
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
        assertThat(loaded?.id).isEqualTo(bankAccount.id)
        assertThat(loaded?.clientId).isEqualTo(bankAccount.clientId)
        assertThat(loaded?.iban).isEqualTo(bankAccount.iban)
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
        assertThat(result?.id).isEqualTo(bankAccountId)
    }

    @Test
    fun `should return null when iban not found`() {
        // Act
        val result = bankAccountRepository.getBankAccountByIban("UNKNOWN")

        // Assert
        assertThat(result).isNull()
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
        assertThat(updated.balance).isEqualByComparingTo("150")
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
        assertThat(updated.balance).isEqualByComparingTo("60")
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
        assertThat(deleted.isEmpty).isTrue()
    }

    @Test
    fun `should throw exception for negative increase amount`() {
        val bankAccountId = UUID.randomUUID().toString()

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
            bankAccountRepository.increaseBankAccountBalance(
                bankAccountId = bankAccountId,
                amount = BigDecimal("-1"),
            )
        }
    }

    @Test
    fun `should return null when account id is not found`() {
        val result = bankAccountRepository.getBankAccountById("non-existent-id")

        assertThat(result).isNull()
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
        assertThat(result).hasSize(2)
        assertThat(result).anyMatch { it.id == firstAccount.id }
        assertThat(result).anyMatch { it.id == secondAccount.id }
    }

    @Test
    fun `should return empty list when client has no accounts`() {
        // Act
        val result = bankAccountRepository.getBankAccountsByClientId("unknown-client")

        // Assert
        assertThat(result).isEmpty()
    }

    @Test
    fun `should throw exception when increasing balance for unknown account`() {
        org.junit.jupiter.api.Assertions.assertThrows(NoSuchElementException::class.java) {
            bankAccountRepository.increaseBankAccountBalance(
                bankAccountId = "unknown-account",
                amount = BigDecimal("10"),
            )
        }
    }

    @Test
    fun `should throw exception for zero increase amount`() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
            bankAccountRepository.increaseBankAccountBalance(
                bankAccountId = UUID.randomUUID().toString(),
                amount = BigDecimal.ZERO,
            )
        }
    }

    @Test
    fun `should throw exception for negative decrease amount`() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
            bankAccountRepository.decreaseBankAccountBalance(
                bankAccountId = UUID.randomUUID().toString(),
                amount = BigDecimal("-1"),
            )
        }
    }

    @Test
    fun `should throw exception for zero decrease amount`() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException::class.java) {
            bankAccountRepository.decreaseBankAccountBalance(
                bankAccountId = UUID.randomUUID().toString(),
                amount = BigDecimal.ZERO,
            )
        }
    }

    @Test
    fun `should throw exception when decreasing balance for unknown account`() {
        org.junit.jupiter.api.Assertions.assertThrows(NoSuchElementException::class.java) {
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
        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException::class.java) {
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
        assertThat(bankAccountJpaRepository.findById(firstAccount.id).isEmpty).isTrue()
        assertThat(bankAccountJpaRepository.findById(secondAccount.id).isEmpty).isTrue()
        assertThat(bankAccountJpaRepository.findById(otherClientAccount.id).isPresent).isTrue()
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
        assertThat(bankAccountJpaRepository.findById(entity.id).isPresent).isTrue()
    }
}
