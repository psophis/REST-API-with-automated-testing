package com.bank.payment.api

import com.bank.payment.domain.Transaction

fun Transaction.toDto(): TransactionDto =
    TransactionDto(
        id = id,
        bankAccountId = accountId,
        amount = amount,
        type = type,
        createdAt = createdAt,
    )
