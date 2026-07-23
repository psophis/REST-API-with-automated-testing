package com.bank.payment.api

import com.bank.payment.application.PaymentAccountNotFoundException
import com.bank.payment.application.PaymentService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.module.kotlin.jacksonMapperBuilder
import java.math.BigDecimal

class PaymentExceptionHandlerTest {
    private val paymentService = mockk<PaymentService>()

    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(PaymentController(paymentService))
            .setControllerAdvice(PaymentExceptionHandler())
            .setMessageConverters(
                JacksonJsonHttpMessageConverter(jacksonMapperBuilder()),
            ).build()

    @Test
    fun `should return 404 for PaymentAccountNotFoundException`() {
        // Arrange
        val fromIban = "DE1234567890"
        val toIban = "DE0987654321"
        val amount = BigDecimal("100.00")

        every {
            paymentService.transferMoney(fromIban, toIban, amount)
        } throws PaymentAccountNotFoundException("Account not found")

        // Act/Assert
        mockMvc
            .post("/api/payments/transfer") {
                contentType = MediaType.APPLICATION_JSON
                content = transferJson(fromIban, toIban, amount)
            }.andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Account not found") }
            }
    }

    @Test
    fun `should return 500 for generic exception`() {
        // Arrange
        val fromIban = "DE1234567890"
        val toIban = "DE0987654321"
        val amount = BigDecimal("100.00")

        every {
            paymentService.transferMoney(fromIban, toIban, amount)
        } throws RuntimeException("boom")

        // Act/Assert
        mockMvc
            .post("/api/payments/transfer") {
                contentType = MediaType.APPLICATION_JSON
                content = transferJson(fromIban, toIban, amount)
            }.andExpect {
                status { isInternalServerError() }
                jsonPath("$.message") { value("boom") }
            }
    }

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
}
