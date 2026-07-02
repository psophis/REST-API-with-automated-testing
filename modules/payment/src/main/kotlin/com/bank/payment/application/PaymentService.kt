package com.bank.payment.application

import com.bank.bankaccount.domain.BankAccountRepository
import com.bank.payment.domain.Transaction
import com.bank.payment.domain.TransactionRepository
import com.bank.payment.domain.TransactionType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import javax.security.auth.login.AccountNotFoundException

@Service
class PaymentService(
    private val transactionRepository: TransactionRepository,
    private val bankAccountRepository: BankAccountRepository,
) {
    @Transactional
    fun transferMoney(
        fromIban: String,
        toIban: String,
        amount: BigDecimal,
    ): Transaction {
        val senderAccount = bankAccountRepository.getBankAccountByIban(fromIban)
        val recipientAccount = bankAccountRepository.getBankAccountByIban(toIban)
        if (senderAccount == null && recipientAccount == null) {
            throw AccountNotFoundException("Account not found for IBAN $fromIban or $toIban")
        }

        val localAccount = senderAccount ?: recipientAccount!!
        val transaction =
            createTransaction(
                localAccount.id,
                fromIban,
                toIban,
                amount,
                TransactionType.TRANSFER,
            )
        transactionRepository.createTransaction(transaction)

        if (senderAccount != null) {
            bankAccountRepository.decreaseBankAccountBalance(
                bankAccountId = senderAccount.id,
                amount = amount,
            )
        } else {
            val localRecipient =
                recipientAccount
                    ?: throw AccountNotFoundException("Account not found for IBAN $fromIban or $toIban")

            bankAccountRepository.increaseBankAccountBalance(
                bankAccountId = localRecipient.id,
                amount = amount,
            )
        }
        return transaction
    }

    @Transactional
    fun withdrawMoney(
        accountId: String,
        amount: BigDecimal,
    ) {
        val iban =
            bankAccountRepository.getBankAccountById(accountId)?.iban
                ?: throw AccountNotFoundException("Account not found for ID: $accountId")

        val transaction =
            createTransaction(
                accountId,
                iban,
                iban,
                amount,
                TransactionType.WITHDRAWAL,
            )

        transactionRepository.createTransaction(transaction)
        bankAccountRepository.decreaseBankAccountBalance(
            bankAccountId = transaction.accountId,
            amount = transaction.amount,
        )
    }

    @Transactional
    fun depositMoney(
        accountId: String,
        amount: BigDecimal,
    ) {
        val iban =
            bankAccountRepository.getBankAccountById(accountId)?.iban
                ?: throw AccountNotFoundException("Account not found for ID: $accountId")

        // TODO sollte das nicht im TransactionService passieren
        val transaction =
            createTransaction(
                accountId,
                iban,
                iban,
                amount,
                TransactionType.DEPOSIT,
            )

        transactionRepository.createTransaction(transaction)
        bankAccountRepository.increaseBankAccountBalance(
            bankAccountId = transaction.accountId,
            amount = transaction.amount,
        )
    }

    private fun createTransaction(
        accountId: String,
        senderIban: String,
        recipientIban: String,
        amount: BigDecimal,
        transactionType: TransactionType,
    ): Transaction =
        Transaction(
            id = generateTransactionId(),
            accountId = accountId,
            senderIban = senderIban,
            recipientIban = recipientIban,
            amount = amount,
            type = transactionType,
            createdAt = Instant.now(),
        )

    private fun generateTransactionId() = UUID.randomUUID().toString()
}
