package com.bank.payment.api

import java.math.BigDecimal

data class WithdrawalRequest(
    val bankAccountId: String,
    val amount: BigDecimal,
)
