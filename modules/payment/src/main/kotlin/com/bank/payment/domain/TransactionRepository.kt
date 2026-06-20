package com.bank.payment.domain

interface TransactionRepository {
    fun getTransactionById(id: String): Transaction

    fun getTransactionsByAccountId(accountId: String): List<Transaction>

    fun createTransaction(transaction: Transaction): Transaction
}
