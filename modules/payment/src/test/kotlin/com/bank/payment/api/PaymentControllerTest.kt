package com.bank.payment.api

import com.bank.payment.application.PaymentService
import com.bank.payment.domain.Transaction
import com.bank.payment.domain.TransactionType
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.Instant
import javax.security.auth.login.AccountNotFoundException

class PaymentControllerTest {
    private val paymentService = mockk<PaymentService>()
    private val paymentController = PaymentController(paymentService)

    @Test
    fun `should send bank transfer`() {
        // Arrange
        val accountId = "account-id"
        val fromIban = "DE1234567890"
        val toIban = "DE0987654321"
        val amount = BigDecimal("100.00")
        val transaction =
            Transaction(
                id = "transaction-id-1",
                accountId = accountId,
                senderIban = fromIban,
                recipientIban = toIban,
                amount = amount,
                type = TransactionType.TRANSFER,
                createdAt = Instant.now(),
            )
        val bankTransferRequest =
            BankTransferRequest(
                senderIban = fromIban,
                recipientIban = toIban,
                amount = amount,
                bankAccountId = accountId,
            )

        every {
            paymentService.transferMoney(
                fromIban,
                toIban,
                amount,
            )
        } returns transaction

        // Act
        val response = paymentController.sendBankTransfer(bankTransferRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body!!.bankAccountId).isEqualTo(transaction.accountId)
        assertThat(response.body!!.amount).isEqualTo(transaction.amount)
        assertThat(response.body!!.type).isEqualTo(TransactionType.TRANSFER)
        verify(exactly = 1) { paymentService.transferMoney(fromIban, toIban, amount) }
    }

    @Test
    fun `should map sent bank transfer to transaction dto`() {
        // Arrange
        val accountId = "account-id"
        val transactionId = "transaction-id"
        val amount = BigDecimal("100.00")
        val createdAt = Instant.now()
        val fromIban = "DE1234567890"
        val toIban = "DE0987654321"
        val transaction =
            Transaction(
                id = transactionId,
                accountId = accountId,
                senderIban = fromIban,
                recipientIban = toIban,
                amount = amount,
                type = TransactionType.TRANSFER,
                createdAt = createdAt,
            )
        val bankTransferRequest =
            BankTransferRequest(
                bankAccountId = accountId,
                amount = amount,
                senderIban = fromIban,
                recipientIban = toIban,
            )

        every { paymentService.transferMoney(fromIban, toIban, amount) } returns transaction

        // Act
        val response = paymentController.sendBankTransfer(bankTransferRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body).isNotNull
        assertThat(response.body!!.id).isEqualTo(transactionId)
        assertThat(response.body!!.bankAccountId).isEqualTo(accountId)
        assertThat(response.body!!.amount).isEqualByComparingTo("100.00")
        assertThat(response.body!!.type).isEqualTo(TransactionType.TRANSFER)
        assertThat(response.body!!.createdAt).isEqualTo(createdAt)
        verify(exactly = 1) { paymentService.transferMoney(fromIban, toIban, amount) }
    }

    @Test
    fun `should return 404 error when sender's bank account is not found`() {
        // Arrange
        val fromIban = "DE1234567890"
        val toIban = "DE0987654321"
        val amount = BigDecimal("100.00")
        val bankTransferRequest =
            BankTransferRequest(
                "account-id",
                amount,
                fromIban,
                toIban,
            )

        every { paymentService.transferMoney(toIban, fromIban, amount) } throws AccountNotFoundException("Account not found for accountId")

        // Act
        val response = paymentController.sendBankTransfer(bankTransferRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        verify(exactly = 1) {
            paymentService.transferMoney(toIban, fromIban, amount)
        }
    }

    @Test
    fun `should return 500 error when transfer cannot be sent`() {
        // Arrange
        val fromIban = "DE0987654321"
        val toIban = "DE1234567890"
        val amount = BigDecimal("100.00")
        val bankTransferRequest =
            BankTransferRequest(
                "account-id",
                amount,
                toIban,
                fromIban,
            )

        every { paymentService.transferMoney(fromIban, toIban, amount) } throws RuntimeException("boom")

        // Act
        val response = paymentController.sendBankTransfer(bankTransferRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        verify(exactly = 1) {
            paymentService.transferMoney(fromIban, toIban, amount)
        }
    }

    @Test
    fun `should withdraw money`() {
        // Arrange
        val accountId = "account-id"
        val amount = BigDecimal("100.00")
        val withdrawalRequest =
            WithdrawalRequest(
                bankAccountId = accountId,
                amount = BigDecimal("100.00"),
            )

        every { paymentService.withdrawMoney(accountId, amount) } just runs

        // Act
        val response = paymentController.withdrawMoney(withdrawalRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        verify { paymentService.withdrawMoney(accountId, amount) }
    }

    @Test
    fun `should return 500 error when money cannot be withdrawn`() {
        // Arrange
        val accountId = "account-id"
        val amount = BigDecimal("100.00")
        val withdrawalRequest =
            WithdrawalRequest(
                bankAccountId = accountId,
                amount = BigDecimal("100.00"),
            )

        every { paymentService.withdrawMoney(accountId, amount) } throws RuntimeException("boom")

        // Act
        val response = paymentController.withdrawMoney(withdrawalRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        verify(exactly = 1) { paymentService.withdrawMoney(accountId, amount) }
    }

    @Test
    fun `should deposit money`() {
        // Arrange
        val accountId = "account-id"
        val amount = BigDecimal("100.00")
        val depositRequest =
            DepositRequest(
                bankAccountId = accountId,
                amount = amount,
            )

        every { paymentService.depositMoney(accountId, amount) } just runs

        // Act
        val response = paymentController.depositMoney(depositRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        verify { paymentService.depositMoney(accountId, amount) }
    }

    @Test
    fun `should return 500 error when money cannot be deposited`() {
        // Arrange
        val accountId = "account-id"
        val amount = BigDecimal("100.00")
        val depositRequest =
            DepositRequest(
                bankAccountId = accountId,
                amount = amount,
            )

        every { paymentService.depositMoney(accountId, amount) } throws RuntimeException("boom")

        // Act
        val response = paymentController.depositMoney(depositRequest)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        verify(exactly = 1) { paymentService.depositMoney(accountId, amount) }
    }
}
