package com.bank.bankaccount.persistence

import com.bank.bankaccount.domain.BankAccount
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
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
@ContextConfiguration(classes = [BankAccountRepositoryImplTest.JpaTestConfig::class])
class BankAccountRepositoryImplTest {
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
        assertEquals(bankAccount.id, loaded.id)
        assertEquals(bankAccount.clientId, loaded.clientId)
        assertEquals(bankAccount.iban, loaded.iban)
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
        assertNotNull(result)
        assertEquals(bankAccountId, result!!.id)
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
}
