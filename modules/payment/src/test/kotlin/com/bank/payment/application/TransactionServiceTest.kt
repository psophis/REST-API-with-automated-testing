package com.bank.payment.application

import com.bank.payment.domain.Transaction
import com.bank.payment.domain.TransactionRepository
import com.bank.payment.domain.TransactionType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
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

        val response = transactionService.getTransaction(transactionId)

        assertThat(response.id).isEqualTo(transactionId)
        assertThat(response.accountId).isEqualTo(transaction.accountId)
        assertThat(response.senderIban).isEqualTo(transaction.senderIban)
        assertThat(response.recipientIban).isEqualTo(transaction.recipientIban)
        assertThat(response.amount).isEqualByComparingTo(transaction.amount)
        assertThat(response.createdAt).isEqualTo(transaction.createdAt)
        assertThat(response.createdAt).isEqualTo(transaction.createdAt)
        verify(exactly = 1) { transactionRepository.getTransactionById(transactionId) }
    }

    @Test
    fun `should throw exception when returning transaction fails`() {
        val transactionId = "transaction-id"
        every { transactionRepository.getTransactionById(transactionId) } throws RuntimeException("boom")

        assertThrows(RuntimeException::class.java) { transactionService.getTransaction(transactionId) }
    }

    @Test
    fun `should get a list of transactions by account id`() {
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

        val response = transactionService.getAccountTransactions(accountId)

        assertThat(response.size).isEqualTo(transactions.size)
        assertThat(response.first().id).isEqualTo(transactions[0].id)
        assertThat(response.first().accountId).isEqualTo(transactions[0].accountId)
        assertThat(response.first().senderIban).isEqualTo(transactions[0].senderIban)
        assertThat(response.first().recipientIban).isEqualTo(transactions[0].recipientIban)
        assertThat(response.first().amount).isEqualByComparingTo(transactions[0].amount)
        assertThat(response.first().createdAt).isEqualTo(transactions[0].createdAt)
        assertThat(response.first().createdAt).isEqualTo(transactions[0].createdAt)
        verify(exactly = 1) { transactionRepository.getTransactionsByAccountId(accountId) }
    }

    @Test
    fun `should throw exception when returning list of transactions fails`() {
        val accountId = "account-id"
        every { transactionRepository.getTransactionsByAccountId(accountId) } throws RuntimeException("boom")

        assertThrows(RuntimeException::class.java) { transactionService.getAccountTransactions(accountId) }
    }

    @Test
    fun `should return an empty list of transactions when account has no transaction`() {
        val accountId = "account-id"
        every { transactionRepository.getTransactionsByAccountId(accountId) } returns emptyList()

        val response = transactionService.getAccountTransactions(accountId)

        assertThat(response).isEmpty()
        verify(exactly = 1) { transactionRepository.getTransactionsByAccountId(accountId) }
    }
}
