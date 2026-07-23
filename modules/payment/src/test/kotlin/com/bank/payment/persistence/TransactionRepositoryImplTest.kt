package com.bank.payment.persistence

import com.bank.payment.domain.Transaction
import com.bank.payment.domain.TransactionType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional

class TransactionRepositoryImplTest {
    private val transactionJpaRepository = mockk<TransactionJpaRepository>()
    private val transactionRepository = TransactionRepositoryImpl(transactionJpaRepository)

    @Test
    fun `should return transaction by id`() {
        // Arrange
        val transactionId = "transaction-id"
        val transaction =
            Transaction(
                id = transactionId,
                accountId = "account-id",
                senderIban = "DE1234567890",
                recipientIban = "DE0987654321",
                amount = BigDecimal("100.00"),
                type = TransactionType.WITHDRAWAL,
                createdAt = Instant.now(),
            )
        val entity =
            TransactionEntity(
                id = transaction.id,
                accountId = transaction.accountId,
                senderIban = transaction.senderIban,
                recipientIban = transaction.recipientIban,
                amount = transaction.amount,
                type = transaction.type,
                createdAt = transaction.createdAt,
            )
        every { transactionJpaRepository.findById(transactionId) } returns Optional.of(entity)

        // Act
        val loaded = transactionRepository.getTransactionById(transactionId)

        // Assert
        assertThat(loaded).isEqualTo(transaction)
        verify(exactly = 1) { transactionJpaRepository.findById(transactionId) }
    }

    @Test
    fun `should throw when transaction is not found`() {
        // Arrange
        val transactionId = "missing-id"
        every { transactionJpaRepository.findById(transactionId) } returns Optional.empty()

        // Act
        val exception =
            assertThrows(NoSuchElementException::class.java) {
                transactionRepository.getTransactionById(transactionId)
            }

        // Assert
        assertThat(exception.message).isEqualTo("Transaction not found: $transactionId")
        verify(exactly = 1) { transactionJpaRepository.findById(transactionId) }
    }

    @Test
    fun `should return transactions by account id`() {
        // Arrange
        val accountId = "account-id"
        val createdAt = Instant.now()
        val entities =
            listOf(
                TransactionEntity(
                    id = "transaction-1",
                    accountId = accountId,
                    senderIban = "DE1234567890",
                    recipientIban = "DE0987654321",
                    amount = BigDecimal("100.00"),
                    type = TransactionType.TRANSFER,
                    createdAt = createdAt,
                ),
                TransactionEntity(
                    id = "transaction-2",
                    accountId = accountId,
                    senderIban = "DE1234567890",
                    recipientIban = "DE1111111111",
                    amount = BigDecimal("50.00"),
                    type = TransactionType.DEPOSIT,
                    createdAt = createdAt,
                ),
            )
        every { transactionJpaRepository.findAllByAccountId(accountId) } returns entities

        // Act
        val loaded = transactionRepository.getTransactionsByAccountId(accountId)

        // Assert
        assertThat(loaded).hasSize(2)
        assertThat(loaded[0].id).isEqualTo(entities[0].id)
        assertThat(loaded[0].amount).isEqualByComparingTo(entities[0].amount)
        assertThat(loaded[1].id).isEqualTo(entities[1].id)
        assertThat(loaded[1].type).isEqualTo(entities[1].type)
        verify(exactly = 1) { transactionJpaRepository.findAllByAccountId(accountId) }
    }

    @Test
    fun `should create transaction`() {
        // Arrange
        val transaction =
            Transaction(
                id = "transaction-id",
                accountId = "account-id",
                senderIban = "DE1234567890",
                recipientIban = "DE0987654321",
                amount = BigDecimal("100.00"),
                type = TransactionType.TRANSFER,
                createdAt = Instant.now(),
            )
        val savedEntity =
            TransactionEntity(
                id = transaction.id,
                accountId = transaction.accountId,
                senderIban = transaction.senderIban,
                recipientIban = transaction.recipientIban,
                amount = transaction.amount,
                type = transaction.type,
                createdAt = transaction.createdAt,
            )
        every { transactionJpaRepository.save(any()) } returns savedEntity

        // Act
        val created = transactionRepository.createTransaction(transaction)

        // Assert
        assertThat(created).isEqualTo(transaction)
        verify(exactly = 1) { transactionJpaRepository.save(any()) }
    }
}
