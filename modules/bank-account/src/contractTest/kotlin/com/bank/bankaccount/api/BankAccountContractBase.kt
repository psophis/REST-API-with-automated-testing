package com.bank.bankaccount.api

import com.bank.bankaccount.application.BankAccountService
import com.bank.bankaccount.domain.BankAccount
import io.mockk.every
import io.mockk.mockk
import io.restassured.module.mockmvc.RestAssuredMockMvc
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.module.kotlin.jacksonMapperBuilder
import java.math.BigDecimal
import java.time.Instant

abstract class BankAccountContractBase {
    lateinit var mockMvc: MockMvc

    private val bankAccountService = mockk<BankAccountService>()

    @BeforeEach
    fun setup() {
        every { bankAccountService.createBankAccount("client-id") } returns
            BankAccount(
                id = "account-id",
                clientId = "client-id",
                iban = "DE02100100100006820101",
                balance = BigDecimal.ZERO,
                createdAt = Instant.parse("2026-07-15T10:00:00Z"),
            )
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(BankAccountController(bankAccountService))
                .setControllerAdvice(BankAccountExceptionHandler())
                .setMessageConverters(JacksonJsonHttpMessageConverter(jacksonMapperBuilder()))
                .build()
        RestAssuredMockMvc.mockMvc(mockMvc)
    }
}
