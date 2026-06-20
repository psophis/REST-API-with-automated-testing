package com.bank.bankaccount.api

import com.bank.bankaccount.application.BankAccountService
import com.bank.bankaccount.createAccount
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal

class AccountControllerTest {
    private val bankAccountService = mockk<BankAccountService>()
    private val bankAccountController = BankAccountController(bankAccountService)

    @Test
    fun `should get account by id`() {
        // Arrange
        val account =
            createAccount(
                balance = BigDecimal("100.00"),
            )

        every { bankAccountService.getBankAccount(account.id) } returns account

        // Act
        val response = bankAccountController.getBankAccount(account.id)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.id).isEqualTo(account.id)
        assertThat(response.body!!.clientId).isEqualTo(account.clientId)
        assertThat(response.body!!.iban).isEqualTo(account.iban)

        verify(exactly = 1) { bankAccountService.getBankAccount(account.id) }
    }

    @Test
    fun `should return 404 if account is not found`() {
        // Arrange
        val accountId = "account-id"

        every { bankAccountService.getBankAccount(accountId) } throws RuntimeException("boom")

        // Act
        val response = bankAccountController.getBankAccount(accountId)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)

        verify(exactly = 1) { bankAccountService.getBankAccount(accountId) }
    }

    @Test
    fun `should create account`() {
        // Arrange
        val request =
            BankAccountRequest(
                clientId = "client-id",
            )

        val account =
            createAccount(
                balance = BigDecimal("100.00"),
            ).copy(
                clientId = "client-id",
            )

        every {
            bankAccountService.createBankAccount(
                request.clientId,
            )
        } returns account

        // Act
        val response = bankAccountController.createBankAccount(request)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body!!.id).isEqualTo(account.id)
        assertThat(response.body!!.clientId).isEqualTo(account.clientId)
        assertThat(response.body!!.iban).isEqualTo(account.iban)

        verify(exactly = 1) {
            bankAccountService.createBankAccount(
                request.clientId,
            )
        }
    }
}
