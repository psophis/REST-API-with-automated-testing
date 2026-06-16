package com.bank.bankaccount

import com.bank.bankaccount.domain.BankAccount
import com.bank.bankaccount.domain.BankAccountType
import java.math.BigDecimal
import java.time.Instant

fun createAccount(
    balance: BigDecimal,
    bankAccountType: BankAccountType,
): BankAccount =
    BankAccount(
        id = "account-id",
        clientId = "client-id",
        iban = "iban",
        balance = balance,
        bankAccountType = bankAccountType,
        createdAt = Instant.now(),
    )
