package com.bank.payment.api

import java.math.BigDecimal

data class DepositRequest(
    val bankAccountId: String,
    val amount: BigDecimal,
)
