package com.bank.bankaccount.domain

import java.math.BigDecimal

interface BankAccountRepository {
    fun getBankAccountById(bankAccountId: String): BankAccount?

    fun getBankAccountByIban(iban: String): BankAccount?

    fun getBankAccountsByClientId(clientId: String): List<BankAccount>

    fun createBankAccount(bankAccount: BankAccount): BankAccount

    fun increaseBankAccountBalance(
        bankAccountId: String,
        amount: BigDecimal,
    )

    fun decreaseBankAccountBalance(
        bankAccountId: String,
        amount: BigDecimal,
    )

    fun deleteByBankAccountId(bankAccountId: String)

    fun deleteBankAccountByClientId(clientId: String)
}
