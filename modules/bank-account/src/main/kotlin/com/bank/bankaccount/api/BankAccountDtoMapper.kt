package com.bank.bankaccount.api

import com.bank.bankaccount.domain.BankAccount

fun BankAccount.toDto(): BankAccountDto =
    BankAccountDto(
        id = id,
        clientId = clientId,
        iban = iban,
        balance = balance,
        createdAt = createdAt,
    )
