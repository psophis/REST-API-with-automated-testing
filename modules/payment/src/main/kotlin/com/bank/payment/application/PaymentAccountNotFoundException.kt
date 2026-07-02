package com.bank.payment.application

class PaymentAccountNotFoundException(
    message: String,
) : RuntimeException(message)
