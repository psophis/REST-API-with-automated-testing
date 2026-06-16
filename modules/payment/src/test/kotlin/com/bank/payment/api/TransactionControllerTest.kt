package com.bank.payment.api

import com.bank.payment.application.TransactionService
import com.bank.payment.domain.Transaction
import com.bank.payment.domain.TransactionType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.Instant

class TransactionControllerTest {
    private val transactionService = mockk<TransactionService>()
    private val transactionController = TransactionController(transactionService)

    @Test
    fun `should get transactions by account id`() {
        // Arrange
        val accountId = "account-id"
        val transaction =
            Transaction(
                id = "transaction-id-1",
                accountId = accountId,
                senderIban = "DE1234567890",
                recipientIban = "DE0987654321",
                amount = BigDecimal("100.00"),
                type = TransactionType.TRANSFER,
                createdAt = Instant.now(),
            )

        every { transactionService.getAccountTransactions(accountId) } returns listOf(transaction)

        // Act
        val response = transactionController.getAccountTransactions(accountId)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.size).isEqualTo(1)
        verify(exactly = 1) { transactionService.getAccountTransactions(accountId) }
    }

    @Test
    fun `should return empty transaction list when account has no transactions`() {
        // Arrange
        val accountId = "account-id"

        every { transactionService.getAccountTransactions(accountId) } returns emptyList()

        // Act
        val response = transactionController.getAccountTransactions(accountId)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.size).isEqualTo(0)
        verify(exactly = 1) { transactionService.getAccountTransactions(accountId) }
    }

    @Test
    fun `should map returned transactions to transaction dto`() {
        // Arrange
        val accountId = "account-id"
        val transactionId = "transaction-id"
        val createdAt = Instant.now()
        val transaction =
            Transaction(
                id = transactionId,
                accountId = accountId,
                senderIban = "DE1234567890",
                recipientIban = "DE0987654321",
                amount = BigDecimal("100.00"),
                type = TransactionType.TRANSFER,
                createdAt = createdAt,
            )

        every { transactionService.getAccountTransactions(accountId) } returns listOf(transaction)

        // Act
        val response = transactionController.getAccountTransactions(accountId)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body).hasSize(1)
        assertThat(response.body!![0].id).isEqualTo(transactionId)
        assertThat(response.body!![0].bankAccountId).isEqualTo(accountId)
        assertThat(response.body!![0].amount).isEqualByComparingTo("100.00")
        assertThat(response.body!![0].type).isEqualTo(TransactionType.TRANSFER)
        assertThat(response.body!![0].createdAt).isEqualTo(createdAt)
        verify(exactly = 1) { transactionService.getAccountTransactions(accountId) }
    }

    @Test
    fun `should return 500 error when transactions cannot be returned`() {
        // Arrange
        val accountId = "account-id"

        every { transactionService.getAccountTransactions(accountId) } throws RuntimeException("boom")

        // Act
        val response = transactionController.getAccountTransactions(accountId)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body).isNull()
        verify(exactly = 1) { transactionService.getAccountTransactions(accountId) }
    }

    @Test
    fun `get transaction by transaction id`() {
        // Arrange
        val transactionId = "transaction-id"
        val transaction =
            Transaction(
                id = transactionId,
                accountId = "account-id",
                senderIban = "DE1234567890",
                recipientIban = "DE0987654321",
                amount = BigDecimal("100.00"),
                type = TransactionType.TRANSFER,
                createdAt = Instant.now(),
            )

        every { transactionService.getTransaction(transactionId) } returns transaction

        // Act
        val response = transactionController.getTransaction(transactionId)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.id).isEqualTo(transaction.id)
        assertThat(response.body!!.bankAccountId).isEqualTo(transaction.accountId)
        assertThat(response.body!!.amount).isEqualTo(transaction.amount)
        assertThat(response.body!!.type).isEqualTo(TransactionType.TRANSFER)
        verify(exactly = 1) { transactionService.getTransaction(transactionId) }
    }

    @Test
    fun `should return 500 error when transaction cannot be returned`() {
        // Arrange
        val transactionId = "transaction-id"

        every { transactionService.getTransaction(transactionId) } throws RuntimeException("boom")

        // Act
        val response = transactionController.getTransaction(transactionId)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body).isNull()
        verify(exactly = 1) { transactionService.getTransaction(transactionId) }
    }
}
