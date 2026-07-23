package com.bank.bankaccount.application

import com.bank.bankaccount.domain.BankAccount
import com.bank.bankaccount.domain.BankAccountRepository
import com.bank.bankaccount.domain.BankAccountTransactionRepository
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

class BankAccountServiceTest {
    private lateinit var bankAccountRepository: BankAccountRepository
    private lateinit var bankAccountTransactionRepository: BankAccountTransactionRepository
    private lateinit var ibanGenerator: IbanGenerator
    private lateinit var bankAccountService: BankAccountService

    @BeforeEach
    fun setup() {
        bankAccountRepository = mockk()
        bankAccountTransactionRepository = mockk()
        ibanGenerator = mockk()
        bankAccountService = BankAccountService(bankAccountRepository, bankAccountTransactionRepository, ibanGenerator)
    }

    @Test
    fun `should get account by account id`() {
        val bankAccount =
            BankAccount(
                id = "account-id",
                clientId = "client-1",
                iban = "DE1234567890",
                balance = BigDecimal.ZERO,
                createdAt = Instant.now(),
            )

        every { bankAccountRepository.getBankAccountById(bankAccount.id) } returns bankAccount

        val result = bankAccountService.getBankAccount(bankAccount.id)

        assertThat(result).isEqualTo(bankAccount)
        verify(exactly = 1) { bankAccountRepository.getBankAccountById(bankAccount.id) }
    }

    @Test
    fun `should throw exception when returning account fails`() {
        val accountId = "account-id"

        every { bankAccountRepository.getBankAccountById(any()) } throws RuntimeException("boom")

        assertThrows(RuntimeException::class.java) { bankAccountService.getBankAccount(accountId) }
    }

    @Test
    fun `should create new account`() {
        val clientId = "client-id"
        val iban = "DE1234567890"

        every { ibanGenerator.generateIban() } returns iban
        every { bankAccountRepository.createBankAccount(any()) } answers { firstArg() }

        val result =
            bankAccountService.createBankAccount(
                clientId = clientId,
            )

        assertThat(result.clientId).isEqualTo(clientId)
        assertThat(result.iban).isEqualTo(iban)
        assertThat(result.balance).isEqualTo(BigDecimal.ZERO)
        verify(exactly = 1) { ibanGenerator.generateIban() }
        verify(exactly = 1) { bankAccountRepository.createBankAccount(any()) }
    }

    @Test
    fun `should throw exception when account creation fails`() {
        every { ibanGenerator.generateIban() } returns "DE1234567890"
        every { bankAccountRepository.createBankAccount(any()) } throws RuntimeException("boom")

        assertThrows(RuntimeException::class.java) {
            bankAccountService.createBankAccount(
                clientId = "client-id",
            )
        }
    }

    @Test
    fun `should delete account by account id`() {
        val accountId = "account-id"
        val account =
            BankAccount(
                id = "account-id",
                clientId = "client-id",
                iban = "DE0123456789",
                balance = BigDecimal.ZERO,
                createdAt = Instant.now(),
            )
        every { bankAccountTransactionRepository.deleteTransactionsByBankAccountId(accountId) } just runs
        every { bankAccountRepository.getBankAccountById(accountId) } returns account
        every { bankAccountRepository.deleteByBankAccountId(accountId) } just runs

        bankAccountService.deleteBankAccount(accountId)

        verify(exactly = 1) { bankAccountRepository.deleteByBankAccountId(accountId) }
        verify(exactly = 1) { bankAccountTransactionRepository.deleteTransactionsByBankAccountId(accountId) }
        verify(exactly = 1) { bankAccountRepository.deleteByBankAccountId(accountId) }
    }

    @Test
    fun `should throw RuntimeException when account deletion fails`() {
        val accountId = "account-id"
        val account =
            BankAccount(
                id = "account-id",
                clientId = "client-id",
                iban = "DE0123456789",
                balance = BigDecimal.ZERO,
                createdAt = Instant.now(),
            )

        every { bankAccountRepository.getBankAccountById(accountId) } returns account
        every { bankAccountTransactionRepository.deleteTransactionsByBankAccountId(accountId) } just runs
        every { bankAccountRepository.deleteByBankAccountId(accountId) } throws RuntimeException("boom")

        val exception =
            assertThrows(RuntimeException::class.java) {
                bankAccountService.deleteBankAccount(accountId)
            }

        assertThat(exception.message).isEqualTo("boom")
        verify(exactly = 1) { bankAccountRepository.getBankAccountById(accountId) }
        verify(exactly = 1) { bankAccountTransactionRepository.deleteTransactionsByBankAccountId(accountId) }
        verify(exactly = 1) { bankAccountRepository.deleteByBankAccountId(accountId) }
    }

    @Test
    fun `should throw BankAccountNotFoundException when account does not exist`() {
        val accountId = "non-existent-account-id"

        every { bankAccountRepository.getBankAccountById(accountId) } returns null

        val exception =
            assertThrows(BankAccountNotFoundException::class.java) {
                bankAccountService.getBankAccount(accountId)
            }

        assertThat(exception.message).isEqualTo("Could not find bank account with id $accountId")
        verify(exactly = 1) { bankAccountRepository.getBankAccountById(accountId) }
    }

    @Test
    fun `should throw exception when client id is blank during creation`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                bankAccountService.createBankAccount(" ")
            }

        assertThat(exception.message).isEqualTo("ClientId cannot be blank")
        verify(exactly = 0) { ibanGenerator.generateIban() }
        verify(exactly = 0) { bankAccountRepository.createBankAccount(any()) }
    }

    @Test
    fun `should throw exception when bank account id is blank during delete`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                bankAccountService.deleteBankAccount(" ")
            }

        assertThat(exception.message).isEqualTo("BankAccountId cannot be blank")
        verify(exactly = 0) { bankAccountRepository.deleteByBankAccountId(any()) }
    }

    @Test
    fun `should throw BankAccountHasNonZeroBalanceException during deletion when balance is non-zero`() {
        val accountId = "account-id"
        val account =
            BankAccount(
                id = accountId,
                clientId = "client-id",
                iban = "DE1234567890",
                balance = BigDecimal("100.00"),
                createdAt = Instant.now(),
            )
        every { bankAccountRepository.getBankAccountById(accountId) } returns account
        every { bankAccountTransactionRepository.deleteTransactionsByBankAccountId(accountId) } just runs
        every { bankAccountRepository.deleteByBankAccountId(accountId) } just runs

        val exception =
            assertThrows(BankAccountHasNonZeroBalanceException::class.java) {
                bankAccountService.deleteBankAccount(accountId)
            }

        assertThat(exception.message).isEqualTo("Bank account with id $accountId has non-zero balance and cannot be deleted")
        verify(exactly = 1) { bankAccountRepository.getBankAccountById(accountId) }
        verify(exactly = 0) { bankAccountTransactionRepository.deleteTransactionsByBankAccountId(accountId) }
        verify(exactly = 0) { bankAccountRepository.deleteByBankAccountId(accountId) }
    }
}
