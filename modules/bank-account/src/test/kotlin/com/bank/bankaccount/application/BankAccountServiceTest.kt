package com.bank.bankaccount.application

import com.bank.bankaccount.domain.BankAccount
import com.bank.bankaccount.domain.BankAccountRepository
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals

class BankAccountServiceTest {
    private lateinit var bankAccountRepository: BankAccountRepository
    private lateinit var ibanGenerator: IbanGenerator
    private lateinit var bankAccountService: BankAccountService

    @BeforeEach
    fun setup() {
        bankAccountRepository = mockk()
        ibanGenerator = mockk()
        bankAccountService = BankAccountService(bankAccountRepository, ibanGenerator)
    }

    @Test
    fun `should get account by account id`() {
        // Arrange
        val bankAccount =
            BankAccount(
                id = "account-id",
                clientId = "client-1",
                iban = "DE1234567890",
                balance = BigDecimal.ZERO,
                createdAt = Instant.now(),
            )

        every { bankAccountRepository.getBankAccountById(bankAccount.id) } returns bankAccount

        // Act
        val result = bankAccountService.getBankAccount(bankAccount.id)

        // Assert
        assertEquals(bankAccount, result)
        verify(exactly = 1) { bankAccountRepository.getBankAccountById(bankAccount.id) }
    }

    @Test
    fun `should throw exception when returning account fails`() {
        // Arrange
        val accountId = "account-id"

        every { bankAccountRepository.getBankAccountById(any()) } throws RuntimeException("boom")

        // Assert
        assertThrows(RuntimeException::class.java) { bankAccountService.getBankAccount(accountId) }
    }

    @Test
    fun `should create new account`() {
        // Arrange
        val clientId = "client-id"
        val iban = "DE1234567890"

        every { ibanGenerator.generateIban() } returns iban
        every { bankAccountRepository.createBankAccount(any()) } answers { firstArg() }

        // Act
        val result =
            bankAccountService.createBankAccount(
                clientId = clientId,
            )

        // Assert
        assertEquals(clientId, result.clientId)
        assertEquals(iban, result.iban)
        assertEquals(BigDecimal.ZERO, result.balance)
        verify(exactly = 1) { ibanGenerator.generateIban() }
        verify(exactly = 1) { bankAccountRepository.createBankAccount(any()) }
    }

    @Test
    fun `should throw exception when account creation fails`() {
        // Arrange
        every { ibanGenerator.generateIban() } returns "DE1234567890"
        every { bankAccountRepository.createBankAccount(any()) } throws RuntimeException("boom")

        // Act/Assert
        assertThrows(RuntimeException::class.java) {
            bankAccountService.createBankAccount(
                clientId = "client-id",
            )
        }
    }

    @Test
    fun `should delete account by account id`() {
        // Arrange
        val accountId = "account-id"

        every { bankAccountRepository.deleteByBankAccountId(accountId) } just runs

        // Act
        bankAccountService.deleteBankAccount(accountId)

        // Assert
        verify(exactly = 1) { bankAccountRepository.deleteByBankAccountId(accountId) }
    }

    @Test
    fun `should throw exception when account deletion fails`() {
        // Arrange
        val accountId = "account-id"

        every { bankAccountRepository.deleteByBankAccountId(accountId) } throws RuntimeException("boom")

        // Act
        val exception =
            assertThrows(RuntimeException::class.java) {
                bankAccountService.deleteBankAccount(accountId)
            }

        // Assert
        assertThat(exception.message).isEqualTo("boom")
        verify(exactly = 1) { bankAccountRepository.deleteByBankAccountId(accountId) }
    }
}
