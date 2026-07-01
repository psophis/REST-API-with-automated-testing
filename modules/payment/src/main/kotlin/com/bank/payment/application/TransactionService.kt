package com.bank.payment.application

import com.bank.payment.domain.Transaction
import com.bank.payment.domain.TransactionRepository
import org.springframework.stereotype.Service

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
) {
    fun getTransaction(transactionId: String): Transaction {
        return transactionRepository.getTransactionById(transactionId)
    }

    fun getAccountTransactions(accountId: String): List<Transaction> {
        return transactionRepository.getTransactionsByAccountId(accountId)
    }
}
