package com.bank.payment.api

import com.bank.payment.domain.Transaction
import com.bank.payment.domain.TransactionType

fun Transaction.toDto(): TransactionDto =
    TransactionDto(
        id = id,
        bankAccountId = accountId,
        amount = amount,
        type = type.toDto(),
        createdAt = createdAt,
    )

private fun TransactionType.toDto(): TransactionTypeDto =
    when (this) {
        TransactionType.WITHDRAWAL -> TransactionTypeDto.WITHDRAWAL
        TransactionType.DEPOSIT -> TransactionTypeDto.DEPOSIT
        TransactionType.TRANSFER -> TransactionTypeDto.TRANSFER
    }
