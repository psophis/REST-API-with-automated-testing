package com.bank.bankaccount.persistence

import com.bank.bankaccount.domain.BankAccount
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional

class BankAccountRepositoryImplTest {
    private val bankAccountJpaRepository = mockk<BankAccountJpaRepository>()
    private val bankAccountRepository = BankAccountRepositoryImpl(bankAccountJpaRepository)

    @Test
    fun `should get bank account by id`() {
        val bankAccount = bankAccount()
        every { bankAccountJpaRepository.findById(bankAccount.id) } returns Optional.of(entity(bankAccount))

        val result = bankAccountRepository.getBankAccountById(bankAccount.id)

        assertThat(result).isEqualTo(bankAccount)
        verify(exactly = 1) { bankAccountJpaRepository.findById(bankAccount.id) }
    }

    @Test
    fun `should return null when bank account by id is not found`() {
        val bankAccountId = "missing-account"
        every { bankAccountJpaRepository.findById(bankAccountId) } returns Optional.empty()

        val result = bankAccountRepository.getBankAccountById(bankAccountId)

        assertThat(result).isNull()
        verify(exactly = 1) { bankAccountJpaRepository.findById(bankAccountId) }
    }

    @Test
    fun `should get bank account by iban`() {
        val bankAccount = bankAccount()
        every { bankAccountJpaRepository.findByIban(bankAccount.iban) } returns entity(bankAccount)

        val result = bankAccountRepository.getBankAccountByIban(bankAccount.iban)

        assertThat(result).isEqualTo(bankAccount)
        verify(exactly = 1) { bankAccountJpaRepository.findByIban(bankAccount.iban) }
    }

    @Test
    fun `should return null when bank account by iban is not found`() {
        val iban = "missing-iban"
        every { bankAccountJpaRepository.findByIban(iban) } returns null

        val result = bankAccountRepository.getBankAccountByIban(iban)

        assertThat(result).isNull()
        verify(exactly = 1) { bankAccountJpaRepository.findByIban(iban) }
    }

    @Test
    fun `should get bank accounts by client id`() {
        val clientId = "client-id"
        val bankAccounts =
            listOf(
                bankAccount(id = "account-1", clientId = clientId, iban = "iban-1"),
                bankAccount(id = "account-2", clientId = clientId, iban = "iban-2"),
            )
        every { bankAccountJpaRepository.findAllByClientId(clientId) } returns bankAccounts.map(::entity)

        val result = bankAccountRepository.getBankAccountsByClientId(clientId)

        assertThat(result).containsExactlyElementsOf(bankAccounts)
        verify(exactly = 1) { bankAccountJpaRepository.findAllByClientId(clientId) }
    }

    @Test
    fun `should create bank account`() {
        val bankAccount = bankAccount()
        every { bankAccountJpaRepository.save(any()) } returns entity(bankAccount)

        val result = bankAccountRepository.createBankAccount(bankAccount)

        assertThat(result).isEqualTo(bankAccount)
        verify(exactly = 1) { bankAccountJpaRepository.save(any()) }
    }

    @Test
    fun `should increase bank account balance`() {
        val bankAccount = bankAccount(balance = BigDecimal("100.00"))
        val entity = entity(bankAccount)
        val savedEntity = slot<BankAccountEntity>()
        every { bankAccountJpaRepository.findById(bankAccount.id) } returns Optional.of(entity)
        every { bankAccountJpaRepository.save(capture(savedEntity)) } answers { savedEntity.captured }

        bankAccountRepository.increaseBankAccountBalance(bankAccount.id, BigDecimal("50.00"))

        assertThat(savedEntity.captured.balance).isEqualByComparingTo("150.00")
        verify(exactly = 1) { bankAccountJpaRepository.findById(bankAccount.id) }
        verify(exactly = 1) { bankAccountJpaRepository.save(entity) }
    }

    @Test
    fun `should throw when increasing balance with non-positive amount`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                bankAccountRepository.increaseBankAccountBalance("account-id", BigDecimal.ZERO)
            }

        assertThat(exception.message).isEqualTo("Amount must be more than zero")
        verify(exactly = 0) { bankAccountJpaRepository.findById(any()) }
        verify(exactly = 0) { bankAccountJpaRepository.save(any()) }
    }

    @Test
    fun `should throw when increasing balance for missing bank account`() {
        val bankAccountId = "missing-account"
        every { bankAccountJpaRepository.findById(bankAccountId) } returns Optional.empty()

        val exception =
            assertThrows(NoSuchElementException::class.java) {
                bankAccountRepository.increaseBankAccountBalance(bankAccountId, BigDecimal("50.00"))
            }

        assertThat(exception.message).isEqualTo("Account not found: $bankAccountId")
        verify(exactly = 1) { bankAccountJpaRepository.findById(bankAccountId) }
        verify(exactly = 0) { bankAccountJpaRepository.save(any()) }
    }

    @Test
    fun `should decrease bank account balance`() {
        val bankAccount = bankAccount(balance = BigDecimal("100.00"))
        val entity = entity(bankAccount)
        val savedEntity = slot<BankAccountEntity>()
        every { bankAccountJpaRepository.findById(bankAccount.id) } returns Optional.of(entity)
        every { bankAccountJpaRepository.save(capture(savedEntity)) } answers { savedEntity.captured }

        bankAccountRepository.decreaseBankAccountBalance(bankAccount.id, BigDecimal("40.00"))

        assertThat(savedEntity.captured.balance).isEqualByComparingTo("60.00")
        verify(exactly = 1) { bankAccountJpaRepository.findById(bankAccount.id) }
        verify(exactly = 1) { bankAccountJpaRepository.save(entity) }
    }

    @Test
    fun `should throw when decreasing balance with non-positive amount`() {
        val exception =
            assertThrows(IllegalArgumentException::class.java) {
                bankAccountRepository.decreaseBankAccountBalance("account-id", BigDecimal.ZERO)
            }

        assertThat(exception.message).isEqualTo("Amount must be more than zero")
        verify(exactly = 0) { bankAccountJpaRepository.findById(any()) }
        verify(exactly = 0) { bankAccountJpaRepository.save(any()) }
    }

    @Test
    fun `should throw when decreasing balance for missing bank account`() {
        val bankAccountId = "missing-account"
        every { bankAccountJpaRepository.findById(bankAccountId) } returns Optional.empty()

        val exception =
            assertThrows(NoSuchElementException::class.java) {
                bankAccountRepository.decreaseBankAccountBalance(bankAccountId, BigDecimal("40.00"))
            }

        assertThat(exception.message).isEqualTo("Account not found: $bankAccountId")
        verify(exactly = 1) { bankAccountJpaRepository.findById(bankAccountId) }
        verify(exactly = 0) { bankAccountJpaRepository.save(any()) }
    }

    @Test
    fun `should throw when decreasing balance below zero`() {
        val bankAccount = bankAccount(balance = BigDecimal("30.00"))
        every { bankAccountJpaRepository.findById(bankAccount.id) } returns Optional.of(entity(bankAccount))

        val exception =
            assertThrows(IllegalStateException::class.java) {
                bankAccountRepository.decreaseBankAccountBalance(bankAccount.id, BigDecimal("40.00"))
            }

        assertThat(exception.message).isEqualTo("Funds must be at least equal to withdrawal amount")
        verify(exactly = 1) { bankAccountJpaRepository.findById(bankAccount.id) }
        verify(exactly = 0) { bankAccountJpaRepository.save(any()) }
    }

    @Test
    fun `should delete bank account by id`() {
        val bankAccountId = "account-id"
        every { bankAccountJpaRepository.deleteById(bankAccountId) } just runs

        bankAccountRepository.deleteByBankAccountId(bankAccountId)

        verify(exactly = 1) { bankAccountJpaRepository.deleteById(bankAccountId) }
    }

    @Test
    fun `should delete bank accounts by client id`() {
        val clientId = "client-id"
        val entities =
            listOf(
                entity(bankAccount(id = "account-1", clientId = clientId, iban = "iban-1")),
                entity(bankAccount(id = "account-2", clientId = clientId, iban = "iban-2")),
            )
        every { bankAccountJpaRepository.findAllByClientId(clientId) } returns entities
        every { bankAccountJpaRepository.delete(any()) } just runs

        bankAccountRepository.deleteBankAccountsByClientId(clientId)

        verify(exactly = 1) { bankAccountJpaRepository.findAllByClientId(clientId) }
        verify(exactly = 1) { bankAccountJpaRepository.delete(entities[0]) }
        verify(exactly = 1) { bankAccountJpaRepository.delete(entities[1]) }
    }

    private fun bankAccount(
        id: String = "account-id",
        clientId: String = "client-id",
        iban: String = "DE1234567890",
        balance: BigDecimal = BigDecimal("100.00"),
        createdAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
    ) = BankAccount(
        id = id,
        clientId = clientId,
        iban = iban,
        balance = balance,
        createdAt = createdAt,
    )

    private fun entity(bankAccount: BankAccount) =
        BankAccountEntity(
            id = bankAccount.id,
            clientId = bankAccount.clientId,
            iban = bankAccount.iban,
            balance = bankAccount.balance,
            createdAt = bankAccount.createdAt,
        )
}
