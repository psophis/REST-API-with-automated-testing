package com.bank.payment.api

import com.bank.payment.application.PaymentService
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.restassured.module.mockmvc.RestAssuredMockMvc
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.module.kotlin.jacksonMapperBuilder
import java.math.BigDecimal

abstract class PaymentContractBase {
    lateinit var mockMvc: MockMvc

    private val paymentService = mockk<PaymentService>()

    @BeforeEach
    fun setup() {
        every {
            paymentService.withdrawMoney("account-id", match { it.compareTo(BigDecimal("100.00")) == 0 })
        } just runs

        mockMvc =
            MockMvcBuilders
                .standaloneSetup(PaymentController(paymentService))
                .setControllerAdvice(PaymentExceptionHandler())
                .setMessageConverters(JacksonJsonHttpMessageConverter(jacksonMapperBuilder()))
                .build()

        RestAssuredMockMvc.mockMvc(mockMvc)
    }
}
