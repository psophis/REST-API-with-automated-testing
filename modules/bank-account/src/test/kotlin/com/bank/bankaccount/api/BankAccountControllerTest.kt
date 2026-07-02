package com.bank.bankaccount.api

import com.bank.bankaccount.application.BankAccountNotFoundException
import com.bank.bankaccount.application.BankAccountService
import com.bank.bankaccount.createAccount
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.module.kotlin.jacksonMapperBuilder
import java.math.BigDecimal

class BankAccountControllerTest {
    private val bankAccountService = mockk<BankAccountService>()
    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(BankAccountController(bankAccountService))
            .setControllerAdvice(BankAccountExceptionHandler())
            .setMessageConverters(
                JacksonJsonHttpMessageConverter(jacksonMapperBuilder()),
            ).build()

    @Test
    fun `should get account by id`() {
        val account = createAccount(balance = BigDecimal("100.00"))
        every { bankAccountService.getBankAccount(account.id) } returns account

        mockMvc
            .perform(get("/api/accounts/{bankAccountId}", account.id))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(account.id))
            .andExpect(jsonPath("$.clientId").value(account.clientId))
            .andExpect(jsonPath("$.iban").value(account.iban))
            .andExpect(jsonPath("$.balance").value(100.00))

        verify(exactly = 1) { bankAccountService.getBankAccount(account.id) }
    }

    @Test
    fun `should return 404 if account is not found`() {
        val accountId = "account-id"
        every { bankAccountService.getBankAccount(accountId) } throws BankAccountNotFoundException(accountId)

        mockMvc
            .perform(get("/api/accounts/{bankAccountId}", accountId))
            .andExpect(status().isNotFound)

        verify(exactly = 1) { bankAccountService.getBankAccount(accountId) }
    }

    @Test
    fun `should create account`() {
        val clientId = "client-id"
        val account =
            createAccount(balance = BigDecimal("100.00"))
                .copy(clientId = clientId)
        every { bankAccountService.createBankAccount(clientId) } returns account

        mockMvc
            .perform(
                post("/api/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"clientId":"$clientId"}"""),
            ).andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(account.id))
            .andExpect(jsonPath("$.clientId").value(account.clientId))
            .andExpect(jsonPath("$.iban").value(account.iban))
            .andExpect(jsonPath("$.balance").value(100.00))

        verify(exactly = 1) { bankAccountService.createBankAccount(clientId) }
    }

    @Test
    fun `should delete account`() {
        val account = createAccount(balance = BigDecimal("100.00"))
        every { bankAccountService.deleteBankAccount(account.id) } just runs

        mockMvc
            .perform(
                delete("/api/accounts/{accountId}", account.id)
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isNoContent)

        verify(exactly = 1) { bankAccountService.deleteBankAccount(account.id) }
    }

    @Test
    fun `should return 400 if request is invalid`() {
        val clientId = "client-id"
        every { bankAccountService.createBankAccount(any()) } throws IllegalArgumentException()

        mockMvc
            .perform(
                post("/api/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"clientId":"$clientId"}"""),
            ).andExpect(status().isBadRequest)

        verify(exactly = 1) { bankAccountService.createBankAccount(any()) }
    }
}
