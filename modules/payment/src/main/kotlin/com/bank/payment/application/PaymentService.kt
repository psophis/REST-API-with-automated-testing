package com.bank.payment.application

import com.bank.bankaccount.domain.BankAccountRepository
import com.bank.payment.domain.Transaction
import com.bank.payment.domain.TransactionRepository
import com.bank.payment.domain.TransactionType
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
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

        val isInternalTransfer =
            senderAccount != null &&
                recipientAccount != null &&
                senderAccount.clientId == recipientAccount.clientId

        return if (isInternalTransfer) {
            val senderTransaction =
                createTransaction(
                    senderAccount.id,
                    fromIban,
                    toIban,
                    amount,
                    TransactionType.TRANSFER,
                )
            val recipientTransaction =
                createTransaction(
                    recipientAccount.id,
                    fromIban,
                    toIban,
                    amount,
                    TransactionType.TRANSFER,
                )

            transactionRepository.createTransaction(senderTransaction)
            transactionRepository.createTransaction(recipientTransaction)

            val bookedAt = Instant.now()
            bankAccountRepository.decreaseBankAccountBalance(
                senderAccount.id,
                senderTransaction.id,
                TransactionType.TRANSFER.toString(),
                amount,
                senderTransaction.createdAt,
                bookedAt,
            )
            bankAccountRepository.increaseBankAccountBalance(
                recipientAccount.id,
                recipientTransaction.id,
                TransactionType.TRANSFER.toString(),
                amount,
                recipientTransaction.createdAt,
                bookedAt,
            )
            senderTransaction
        } else {
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

            val bookedAt = Instant.now()
            if (senderAccount != null) {
                bankAccountRepository.decreaseBankAccountBalance(
                    senderAccount.id,
                    transaction.id,
                    TransactionType.TRANSFER.toString(),
                    amount,
                    transaction.createdAt,
                    bookedAt,
                )
            } else {
                val localRecipient =
                    recipientAccount
                        ?: throw AccountNotFoundException("Account not found for IBAN $fromIban or $toIban")

                bankAccountRepository.increaseBankAccountBalance(
                    localRecipient.id,
                    transaction.id,
                    TransactionType.TRANSFER.toString(),
                    amount,
                    transaction.createdAt,
                    bookedAt,
                )
            }
            transaction
        }
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

        try {
            transactionRepository.createTransaction(transaction)
            val bookedAt = Instant.now()
            bankAccountRepository.decreaseBankAccountBalance(
                transaction.accountId,
                transaction.id,
                TransactionType.WITHDRAWAL.toString(),
                transaction.amount,
                transaction.createdAt,
                bookedAt,
            )
        } catch (e: Exception) {
            throw RuntimeException("Error withdrawing money", e)
        }
    }

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

        try {
            transactionRepository.createTransaction(transaction)
            val bookedAt = Instant.now()
            bankAccountRepository.increaseBankAccountBalance(
                transaction.accountId,
                generateTransactionId(),
                TransactionType.DEPOSIT.toString(),
                transaction.amount,
                transaction.createdAt,
                bookedAt,
            )
        } catch (e: Exception) {
            throw RuntimeException("Error depositing money", e)
        }
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
