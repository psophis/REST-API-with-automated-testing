package com.bank.payment.api

import com.bank.payment.domain.TransactionType
import java.math.BigDecimal
import java.time.Instant

data class TransactionDto(
    val id: String,
    val bankAccountId: String,
    val amount: BigDecimal,
    val type: TransactionType,
    val createdAt: Instant,
)
