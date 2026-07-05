package com.bank.bankaccount.domain

interface BankAccountTransactionRepository {
    fun deleteTransactionsByBankAccountId(bankAccountId: String)
}