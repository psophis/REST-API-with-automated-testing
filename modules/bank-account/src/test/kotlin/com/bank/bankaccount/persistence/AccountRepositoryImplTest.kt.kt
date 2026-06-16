package com.bank.bankaccount.persistence

import com.bank.bankaccount.domain.BankAccount
import com.bank.bankaccount.domain.BankAccountType
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
@ContextConfiguration(classes = [AccountRepositoryImplTest.JpaTestConfig::class])
class AccountRepositoryImplTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("com.bank.account.persistence")
    @EnableJpaRepositories("com.bank.bankaccount.persistence")
    class JpaTestConfig

    @Autowired
    lateinit var accountRepository: BankAccountRepositoryImpl

    @Autowired
    lateinit var bankAccountJpaRepository: BankAccountJpaRepository

    val iban = "DE1234567890"
    val accountId = UUID.randomUUID().toString()

    @Test
    fun `should create and load account`() {
        // Arrange
        val bankAccount =
            BankAccount(
                id = accountId,
                clientId = "client-1",
                iban = iban,
                balance = BigDecimal.ZERO,
                bankAccountType = BankAccountType.CHECKING_ACCOUNT,
                createdAt = Instant.now(),
            )
        accountRepository.createBankAccount(bankAccount)

        // Act
        val loaded = accountRepository.getBankAccountById(accountId)

        // Assert
        assertEquals(bankAccount.id, loaded.id)
        assertEquals(bankAccount.clientId, loaded.clientId)
        assertEquals(bankAccount.iban, loaded.iban)
    }

    @Test
    fun `should find account by iban`() {
        // Arrange
        val entity =
            BankAccountEntity(
                id = accountId,
                clientId = "client-1",
                iban = iban,
                balance = BigDecimal.ZERO,
                bankAccountType = BankAccountType.CHECKING_ACCOUNT,
                createdAt = Instant.now(),
            )

        bankAccountJpaRepository.save(entity)

        // Act
        val result = accountRepository.getBankAccountByIban(iban)

        // Assert
        assertNotNull(result)
        assertEquals(accountId, result!!.id)
    }

    @Test
    fun `should return null when iban not found`() {
        // Act
        val result = accountRepository.getBankAccountByIban("UNKNOWN")

        // Assert
        assertNull(result)
    }

    @Test
    fun `should increase account balance`() {
        // Arrange
        val entity =
            BankAccountEntity(
                id = accountId,
                clientId = "client-1",
                iban = iban,
                balance = BigDecimal("100"),
                bankAccountType = BankAccountType.CHECKING_ACCOUNT,
                createdAt = Instant.now(),
            )

        bankAccountJpaRepository.save(entity)

        // Act
        accountRepository.increaseBankAccountBalance(
            bankAccountId = accountId,
            transactionId = "tx-1",
            transactionType = "DEPOSIT",
            amount = BigDecimal("50"),
            createdAt = Instant.now(),
            bookedAt = Instant.now(),
        )

        // Assert
        val updated = bankAccountJpaRepository.findById(accountId).get()
        assertEquals(BigDecimal("150"), updated.balance)
    }

    @Test
    fun `should reduce account balance`() {
        // Arrange
        val entity =
            BankAccountEntity(
                id = accountId,
                clientId = "client-1",
                iban = iban,
                balance = BigDecimal("100"),
                bankAccountType = BankAccountType.CHECKING_ACCOUNT,
                createdAt = Instant.now(),
            )

        bankAccountJpaRepository.save(entity)

        // Act
        accountRepository.decreaseBankAccountBalance(
            bankAccountId = accountId,
            transactionId = "tx-1",
            transactionType = "WITHDRAWAL",
            amount = BigDecimal("40"),
            createdAt = Instant.now(),
            bookedAt = Instant.now(),
        )

        // Assert
        val updated = bankAccountJpaRepository.findById(accountId).get()
        assertEquals(BigDecimal("60"), updated.balance)
    }

    @Test
    fun `should delete account`() {
        // Arrange
        val entity =
            BankAccountEntity(
                id = accountId,
                clientId = "client-1",
                iban = iban,
                balance = BigDecimal.ZERO,
                bankAccountType = BankAccountType.CHECKING_ACCOUNT,
                createdAt = Instant.now(),
            )

        bankAccountJpaRepository.save(entity)

        // Act
        accountRepository.deleteByBankAccountId(accountId)

        val deleted = bankAccountJpaRepository.findById(accountId)

        // Assert
        assertTrue(deleted.isEmpty)
    }

    @Test
    fun `should throw exception for negative increase amount`() {
        assertThrows(IllegalArgumentException::class.java) {
            accountRepository.increaseBankAccountBalance(
                bankAccountId = accountId,
                transactionId = "tx-1",
                transactionType = "DEPOSIT",
                amount = BigDecimal("-1"),
                createdAt = Instant.now(),
                bookedAt = Instant.now(),
            )
        }
    }
}
