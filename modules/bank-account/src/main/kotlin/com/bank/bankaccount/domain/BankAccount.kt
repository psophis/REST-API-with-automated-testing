package com.bank.bankaccount.domain

import java.math.BigDecimal
import java.time.Instant

data class BankAccount(
    val id: String,
    val clientId: String,
    val iban: String,
    val balance: BigDecimal,
    val bankAccountType: BankAccountType,
    val createdAt: Instant,
)
