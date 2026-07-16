package com.bank.bankaccount.api

import com.bank.bankaccount.application.BankAccountService
import com.bank.bankaccount.domain.BankAccount
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.Instant

@WebMvcTest(controllers = [BankAccountController::class])
@Import(BankAccountController::class, BankAccountExceptionHandler::class)
class BankAccountControllerIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var bankAccountService: BankAccountService

    @Test
    fun `should create bank account`() {
        `when`(bankAccountService.createBankAccount("client-id"))
            .thenReturn(
                BankAccount(
                    id = "account-id",
                    clientId = "client-id",
                    iban = "DE02100100100006820101",
                    balance = BigDecimal.ZERO,
                    createdAt = Instant.parse("2026-07-15T10:00:00Z"),
                ),
            )

        mockMvc
            .perform(
                post("/api/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"clientId":"client-id"}"""),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value("account-id"))
            .andExpect(jsonPath("$.clientId").value("client-id"))
            .andExpect(jsonPath("$.iban").value("DE02100100100006820101"))
            .andExpect(jsonPath("$.balance").value(0))
    }

    @SpringBootConfiguration
    class TestApplication
}
