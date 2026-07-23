package com.bank.payment.application

import com.bank.bankaccount.domain.BankAccount
import com.bank.bankaccount.domain.BankAccountRepository
import com.bank.payment.domain.TransactionRepository
import com.bank.payment.domain.TransactionType
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class PaymentServiceTest {
    private lateinit var paymentService: PaymentService
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var bankAccountRepository: BankAccountRepository

    @BeforeEach
    fun setUp() {
        transactionRepository = mockk()
        bankAccountRepository = mockk()
        paymentService = PaymentService(transactionRepository, bankAccountRepository)
    }

    @Test
    fun `should transfer money to external account and decrease balance`() {
        val amount = BigDecimal("100.00")
        val senderAccount = account(id = "sender-account", clientId = "client-a", iban = "DE1234567890")

        every { bankAccountRepository.getBankAccountByIban(senderAccount.iban) } returns senderAccount
        every { bankAccountRepository.getBankAccountByIban("DE0987654321") } returns null
        every { transactionRepository.createTransaction(any()) } answers { firstArg() }
        every {
            bankAccountRepository.decreaseBankAccountBalance(
                bankAccountId = senderAccount.id,
                amount = amount,
            )
        } just runs

        val result = paymentService.transferMoney(senderAccount.iban, "DE0987654321", amount)

        assertThat(result.accountId).isEqualTo(senderAccount.id)
        assertThat(result.senderIban).isEqualTo(senderAccount.iban)
        assertThat(result.recipientIban).isEqualTo("DE0987654321")
        assertThat(result.type).isEqualTo(TransactionType.TRANSFER)

        verify(exactly = 1) { transactionRepository.createTransaction(result) }
        verify(exactly = 1) {
            bankAccountRepository.decreaseBankAccountBalance(
                bankAccountId = senderAccount.id,
                amount = amount,
            )
        }
        verify(exactly = 0) {
            bankAccountRepository.increaseBankAccountBalance(any(), any())
        }
    }

    @Test
    fun `should receive money via bank transfer`() {
        val amount = BigDecimal("100.00")
        val recipientAccount = account(id = "recipient-account", clientId = "client-b", iban = "DE0987654321")

        every { bankAccountRepository.getBankAccountByIban("DE1234567890") } returns null
        every { bankAccountRepository.getBankAccountByIban(recipientAccount.iban) } returns recipientAccount
        every { transactionRepository.createTransaction(any()) } answers { firstArg() }
        every {
            bankAccountRepository.increaseBankAccountBalance(
                bankAccountId = recipientAccount.id,
                amount = amount,
            )
        } just runs

        val result = paymentService.transferMoney("DE1234567890", recipientAccount.iban, amount)

        assertThat(result.accountId).isEqualTo(recipientAccount.id)
        assertThat(result.senderIban).isEqualTo("DE1234567890")
        assertThat(result.recipientIban).isEqualTo(recipientAccount.iban)
        assertThat(result.type).isEqualTo(TransactionType.TRANSFER)

        verify(exactly = 1) { transactionRepository.createTransaction(result) }
        verify(exactly = 1) {
            bankAccountRepository.increaseBankAccountBalance(
                bankAccountId = recipientAccount.id,
                amount = amount,
            )
        }
        verify(exactly = 0) {
            bankAccountRepository.decreaseBankAccountBalance(any(), any())
        }
    }

    @Test
    fun `should throw when no account matches sender or recipient iban`() {
        every { bankAccountRepository.getBankAccountByIban("DE1234567890") } returns null
        every { bankAccountRepository.getBankAccountByIban("DE0987654321") } returns null

        assertThatThrownBy {
            paymentService.transferMoney("DE1234567890", "DE0987654321", BigDecimal("100.00"))
        }.isInstanceOf(PaymentAccountNotFoundException::class.java)
            .hasMessage("Account not found for IBAN DE1234567890 or DE0987654321")
    }

    private fun account(
        id: String,
        clientId: String,
        iban: String,
    ) = BankAccount(
        id = id,
        clientId = clientId,
        iban = iban,
        balance = BigDecimal("1000.00"),
        createdAt = Instant.now(),
    )
}
