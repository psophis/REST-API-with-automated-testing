package com.bank.bankaccount.domain

import java.math.BigDecimal
import java.time.Instant

interface BankAccountRepository {
    fun getBankAccountById(bankAccountId: String): BankAccount?

    fun getBankAccountByIban(iban: String): BankAccount?

    fun getBankAccountsByClientId(clientId: String): List<BankAccount>

    fun createBankAccount(bankAccount: BankAccount): BankAccount

    fun increaseBankAccountBalance(
        bankAccountId: String,
        transactionId: String,
        transactionType: String,
        amount: BigDecimal,
        createdAt: Instant,
        bookedAt: Instant,
    )

    fun decreaseBankAccountBalance(
        bankAccountId: String,
        transactionId: String,
        transactionType: String,
        amount: BigDecimal,
        createdAt: Instant,
        bookedAt: Instant,
    )

    fun deleteByBankAccountId(bankAccountId: String)

    fun deleteBankAccountByClientId(clientId: String)
}
