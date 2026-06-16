package com.bank.payment.api

import java.math.BigDecimal

data class BankTransferRequest(
    val amount: BigDecimal,
    val recipientIban: String,
    val senderIban: String,
)
