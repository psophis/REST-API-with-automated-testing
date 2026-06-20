package com.bank.bankaccount

import com.bank.bankaccount.domain.BankAccount
import java.math.BigDecimal
import java.time.Instant

fun createAccount(
    balance: BigDecimal,
): BankAccount =
    BankAccount(
        id = "account-id",
        clientId = "client-id",
        iban = "iban",
        balance = balance,
        createdAt = Instant.now(),
    )
