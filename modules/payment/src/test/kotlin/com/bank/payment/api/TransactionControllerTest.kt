package com.bank.payment.api

import com.bank.payment.application.TransactionService
import com.bank.payment.domain.Transaction
import com.bank.payment.domain.TransactionType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.module.kotlin.jacksonMapperBuilder
import java.math.BigDecimal
import java.time.Instant

class TransactionControllerTest {
    private val transactionService = mockk<TransactionService>()
    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(TransactionController(transactionService))
            .setControllerAdvice(PaymentExceptionHandler())
            .setMessageConverters(JacksonJsonHttpMessageConverter(jacksonMapperBuilder()))
            .build()

    @Test
    fun `should get transactions by account id`() {
        val accountId = "account-id"
        val transaction = transaction(accountId = accountId)
        every { transactionService.getAccountTransactions(accountId) } returns listOf(transaction)

        mockMvc
            .get("/api/transactions/{accountId}/transactions", accountId)
            .andExpect {
                status { isOk() }
                jsonPath("$[0].id") { value(transaction.id) }
                jsonPath("$[0].bankAccountId") { value(accountId) }
                jsonPath("$[0].amount") { value(100.00) }
                jsonPath("$[0].type") { value(TransactionType.TRANSFER.name) }
                jsonPath("$[0].createdAt") { value(transaction.createdAt.toString()) }
            }

        verify(exactly = 1) { transactionService.getAccountTransactions(accountId) }
    }

    @Test
    fun `should return empty transaction list when account has no transactions`() {
        val accountId = "account-id"
        every { transactionService.getAccountTransactions(accountId) } returns emptyList()

        mockMvc
            .get("/api/transactions/{accountId}/transactions", accountId)
            .andExpect {
                status { isOk() }
                jsonPath("$") { isEmpty() }
            }

        verify(exactly = 1) { transactionService.getAccountTransactions(accountId) }
    }

    @Test
    fun `should return 500 error when transactions cannot be returned`() {
        val accountId = "account-id"
        every { transactionService.getAccountTransactions(accountId) } throws RuntimeException("boom")

        mockMvc
            .get("/api/transactions/{accountId}/transactions", accountId)
            .andExpect {
                status { isInternalServerError() }
            }

        verify(exactly = 1) { transactionService.getAccountTransactions(accountId) }
    }

    @Test
    fun `should get transaction by transaction id`() {
        val transactionId = "transaction-id"
        val transaction = transaction(id = transactionId)
        every { transactionService.getTransaction(transactionId) } returns transaction

        mockMvc
            .get("/api/transactions/{transactionId}", transactionId)
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(transaction.id) }
                jsonPath("$.bankAccountId") { value(transaction.accountId) }
                jsonPath("$.amount") { value(100.00) }
                jsonPath("$.type") { value(TransactionType.TRANSFER.name) }
                jsonPath("$.createdAt") { value(transaction.createdAt.toString()) }
            }

        verify(exactly = 1) { transactionService.getTransaction(transactionId) }
    }

    @Test
    fun `should return 500 error when transaction cannot be returned`() {
        val transactionId = "transaction-id"
        every { transactionService.getTransaction(transactionId) } throws RuntimeException("boom")

        mockMvc
            .get("/api/transactions/{transactionId}", transactionId)
            .andExpect {
                status { isInternalServerError() }
            }

        verify(exactly = 1) { transactionService.getTransaction(transactionId) }
    }

    private fun transaction(
        id: String = "transaction-id",
        accountId: String = "account-id",
    ) = Transaction(
        id = id,
        accountId = accountId,
        senderIban = "DE1234567890",
        recipientIban = "DE0987654321",
        amount = BigDecimal("100.00"),
        type = TransactionType.TRANSFER,
        createdAt = Instant.parse("2026-06-21T12:00:00Z"),
    )
}
