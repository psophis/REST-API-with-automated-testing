package com.bank.bankaccount.api

import com.bank.bankaccount.application.BankAccountNotFoundException
import com.bank.bankaccount.application.BankAccountService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.module.kotlin.jacksonMapperBuilder

class BankAccountExceptionHandlerTest {
    private val bankAccountService = mockk<BankAccountService>()
    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(BankAccountController(bankAccountService))
            .setControllerAdvice(BankAccountExceptionHandler())
            .setMessageConverters(
                JacksonJsonHttpMessageConverter(jacksonMapperBuilder()),
            ).build()

    @Test
    fun `should return 404 for BankAccountNotFoundException`() {
        val accountId = "non-existent-account-id"
        every { bankAccountService.getBankAccount(accountId) } throws BankAccountNotFoundException(accountId)

        mockMvc
            .perform(get("/api/accounts/{bankAccountId}", accountId))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return 400 for IllegalArgumentException`() {
        every { bankAccountService.createBankAccount("") } throws IllegalArgumentException("ClientId cannot be blank")

        mockMvc
            .perform(
                post("/api/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"clientId":""}"""),
            ).andExpect(status().isBadRequest)
    }
}
