package com.bank.payment.application

import com.bank.payment.domain.Transaction
import com.bank.payment.domain.TransactionRepository
import org.springframework.stereotype.Service

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
) {
    fun getTransaction(transactionId: String): Transaction {
        try {
            return transactionRepository.getTransactionById(transactionId)
        } catch (e: Exception) {
            throw e
        }
    }

    fun getAccountTransactions(accountId: String): List<Transaction> {
        try {
            return transactionRepository.getTransactionsByAccountId(accountId)
        } catch (e: Exception) {
            throw e
        }
    }
}
