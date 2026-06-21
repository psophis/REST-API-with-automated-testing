package com.bank.payment.api

import com.bank.payment.application.PaymentService
import com.bank.payment.domain.Transaction
import com.bank.payment.domain.TransactionType
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.module.kotlin.jacksonMapperBuilder
import java.math.BigDecimal
import java.time.Instant
import javax.security.auth.login.AccountNotFoundException

class PaymentControllerTest {
    private val paymentService = mockk<PaymentService>()
    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(PaymentController(paymentService))
            .setMessageConverters(JacksonJsonHttpMessageConverter(jacksonMapperBuilder()))
            .build()

    @Test
    fun `should send bank transfer`() {
        val fromIban = "DE1234567890"
        val toIban = "DE0987654321"
        val amount = BigDecimal("100.00")
        val transaction = transaction(senderIban = fromIban, recipientIban = toIban)
        every { paymentService.transferMoney(fromIban, toIban, amount) } returns transaction

        mockMvc
            .perform(
                post("/api/payments/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(transferJson(fromIban, toIban, amount)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(transaction.id))
            .andExpect(jsonPath("$.bankAccountId").value(transaction.accountId))
            .andExpect(jsonPath("$.amount").value(100.00))
            .andExpect(jsonPath("$.type").value(TransactionType.TRANSFER.name))
            .andExpect(jsonPath("$.createdAt").value(transaction.createdAt.toString()))

        verify(exactly = 1) { paymentService.transferMoney(fromIban, toIban, amount) }
    }

    @Test
    fun `should return 404 error when sender's bank account is not found`() {
        val fromIban = "DE1234567890"
        val toIban = "DE0987654321"
        val amount = BigDecimal("100.00")
        every {
            paymentService.transferMoney(fromIban, toIban, amount)
        } throws AccountNotFoundException("Account not found")

        mockMvc
            .perform(
                post("/api/payments/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(transferJson(fromIban, toIban, amount)),
            ).andExpect(status().isNotFound)

        verify(exactly = 1) { paymentService.transferMoney(fromIban, toIban, amount) }
    }

    @Test
    fun `should return 500 error when transfer cannot be sent`() {
        val fromIban = "DE1234567890"
        val toIban = "DE0987654321"
        val amount = BigDecimal("100.00")
        every { paymentService.transferMoney(fromIban, toIban, amount) } throws RuntimeException("boom")

        mockMvc
            .perform(
                post("/api/payments/transfer")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(transferJson(fromIban, toIban, amount)),
            ).andExpect(status().isInternalServerError)

        verify(exactly = 1) { paymentService.transferMoney(fromIban, toIban, amount) }
    }

    @Test
    fun `should withdraw money`() {
        val accountId = "account-id"
        val amount = BigDecimal("100.00")
        every { paymentService.withdrawMoney(accountId, amount) } just runs

        mockMvc
            .perform(
                post("/api/payments/withdrawal")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(paymentJson(accountId, amount)),
            ).andExpect(status().isOk)

        verify(exactly = 1) { paymentService.withdrawMoney(accountId, amount) }
    }

    @Test
    fun `should return 500 error when money cannot be withdrawn`() {
        val accountId = "account-id"
        val amount = BigDecimal("100.00")
        every { paymentService.withdrawMoney(accountId, amount) } throws RuntimeException("boom")

        mockMvc
            .perform(
                post("/api/payments/withdrawal")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(paymentJson(accountId, amount)),
            ).andExpect(status().isInternalServerError)

        verify(exactly = 1) { paymentService.withdrawMoney(accountId, amount) }
    }

    @Test
    fun `should deposit money`() {
        val accountId = "account-id"
        val amount = BigDecimal("100.00")
        every { paymentService.depositMoney(accountId, amount) } just runs

        mockMvc
            .perform(
                post("/api/payments/deposit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(paymentJson(accountId, amount)),
            ).andExpect(status().isOk)

        verify(exactly = 1) { paymentService.depositMoney(accountId, amount) }
    }

    @Test
    fun `should return 500 error when money cannot be deposited`() {
        val accountId = "account-id"
        val amount = BigDecimal("100.00")
        every { paymentService.depositMoney(accountId, amount) } throws RuntimeException("boom")

        mockMvc
            .perform(
                post("/api/payments/deposit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(paymentJson(accountId, amount)),
            ).andExpect(status().isInternalServerError)

        verify(exactly = 1) { paymentService.depositMoney(accountId, amount) }
    }

    private fun transaction(
        senderIban: String,
        recipientIban: String,
    ) = Transaction(
        id = "transaction-id",
        accountId = "account-id",
        senderIban = senderIban,
        recipientIban = recipientIban,
        amount = BigDecimal("100.00"),
        type = TransactionType.TRANSFER,
        createdAt = Instant.parse("2026-06-21T12:00:00Z"),
    )

    private fun transferJson(
        senderIban: String,
        recipientIban: String,
        amount: BigDecimal,
    ) = """
        {
          "senderIban": "$senderIban",
          "recipientIban": "$recipientIban",
          "amount": $amount
        }
        """.trimIndent()

    private fun paymentJson(
        accountId: String,
        amount: BigDecimal,
    ) = """
        {
          "bankAccountId": "$accountId",
          "amount": $amount
        }
        """.trimIndent()
}
