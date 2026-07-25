package com.bank.bankaccount.api

import java.math.BigDecimal
import java.time.Instant

data class BankAccountDto(
    val id: String,
    val clientId: String,
    val iban: String,
    val balance: BigDecimal,
    val createdAt: Instant,
)
