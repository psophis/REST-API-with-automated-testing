package com.bank.payment.api

import java.math.BigDecimal
import java.time.Instant

data class TransactionDto(
    val id: String,
    val bankAccountId: String,
    val amount: BigDecimal,
    val type: TransactionTypeDto,
    val createdAt: Instant,
)
