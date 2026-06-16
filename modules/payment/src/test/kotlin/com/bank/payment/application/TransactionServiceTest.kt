package com.bank.payment.application

import com.bank.payment.domain.Transaction
import com.bank.payment.domain.TransactionRepository
import com.bank.payment.domain.TransactionType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class TransactionServiceTest {
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var transactionService: TransactionService

    @BeforeEach
    fun setup() {
        transactionRepository = mockk()
        transactionService = TransactionService(transactionRepository)
    }

    @Test
    fun `should get a transaction by transaction id`() {
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

        every { transactionRepository.getTransactionById(transactionId) } returns transaction

        // Act
        val response = transactionService.getTransaction(transactionId)

        // Assert
        assertEquals(transactionId, response.id)
        assertEquals(transaction.accountId, response.accountId)
        assertEquals(transaction.senderIban, response.senderIban)
        assertEquals(transaction.recipientIban, response.recipientIban)
        assertEquals(transaction.amount, response.amount)
        assertEquals(transaction.createdAt, response.createdAt)
        assertEquals(transaction.createdAt, response.createdAt)
        verify(exactly = 1) { transactionRepository.getTransactionById(transactionId) }
    }

    @Test
    fun `should throw exception when returning transaction fails`() {
        // Arrange
        val transactionId = "transaction-id"
        every { transactionRepository.getTransactionById(transactionId) } throws RuntimeException("boom")

        // Assert
        assertThrows(RuntimeException::class.java) { transactionService.getTransaction(transactionId) }
    }

    @Test
    fun `should get a list of transactions by account id`() {
        // Arrange
        val accountId = "account-id"
        val transactions =
            listOf(
                Transaction(
                    id = "transaction-id-1",
                    accountId = accountId,
                    senderIban = "DE1234567890",
                    recipientIban = "DE0987654321",
                    amount = BigDecimal("100.00"),
                    type = TransactionType.WITHDRAWAL,
                    createdAt = Instant.now(),
                ),
            )
        every { transactionRepository.getTransactionsByAccountId(accountId) } returns transactions

        // Act
        val response = transactionService.getAccountTransactions(accountId)

        // Assert
        assertEquals(transactions.size, response.size)
        assertEquals(transactions[0].id, response.first().id)
        assertEquals(transactions[0].accountId, response.first().accountId)
        assertEquals(transactions[0].senderIban, response.first().senderIban)
        assertEquals(transactions[0].recipientIban, response.first().recipientIban)
        assertEquals(transactions[0].amount, response.first().amount)
        assertEquals(transactions[0].createdAt, response.first().createdAt)
        assertEquals(transactions[0].createdAt, response.first().createdAt)
        verify(exactly = 1) { transactionRepository.getTransactionsByAccountId(accountId) }
    }

    @Test
    fun `should throw exception when returning list of transactions fails`() {
        // Arrange
        val accountId = "account-id"
        every { transactionRepository.getTransactionsByAccountId(accountId) } throws RuntimeException("boom")

        // Assert
        assertThrows(RuntimeException::class.java) { transactionService.getAccountTransactions(accountId) }
    }

    @Test
    fun `should return an empty list of transactions when account has no transaction`() {
        // Arrange
        val accountId = "account-id"
        every { transactionRepository.getTransactionsByAccountId(accountId) } returns emptyList()

        // Act
        val response = transactionService.getAccountTransactions(accountId)

        // Assert
        assert(response.isEmpty())
        verify(exactly = 1) { transactionRepository.getTransactionsByAccountId(accountId) }
    }
}
