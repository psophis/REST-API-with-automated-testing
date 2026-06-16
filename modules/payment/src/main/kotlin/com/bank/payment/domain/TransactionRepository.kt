package com.bank.payment.domain

import org.springframework.stereotype.Repository

@Repository
interface TransactionRepository {
    fun getTransactionById(id: String): Transaction

    fun getTransactionsByAccountId(accountId: String): List<Transaction>

    fun createTransaction(transaction: Transaction): Transaction
}
