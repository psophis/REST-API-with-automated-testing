package com.bank.client.api

import com.bank.bankaccount.domain.BankAccount
import com.bank.client.domain.Client

fun Client.toDto(): ClientUpdateRequest =
    ClientUpdateRequest(
        id = id,
        name = name,
        address = address,
    )

fun BankAccount.toDto(): ClientAccountResponse =
    ClientAccountResponse(
        id = id,
        clientId = clientId,
        iban = iban,
        balance = balance,
        createdAt = createdAt,
    )
