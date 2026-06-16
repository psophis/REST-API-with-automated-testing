package com.bank.bankaccount.api

import com.bank.bankaccount.domain.BankAccountType
import java.math.BigDecimal
import java.time.Instant

data class BankAccountDto(
    val id: String,
    val clientId: String,
    val iban: String,
    val balance: BigDecimal,
    val bankAccountType: BankAccountType,
    val createdAt: Instant,
)
