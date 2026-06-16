package com.bank.bankaccount.api

import com.bank.bankaccount.domain.BankAccountType

data class BankAccountRequest(
    val clientId: String,
    val bankAccountType: BankAccountType,
)
