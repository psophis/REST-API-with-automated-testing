package com.bank.bankaccount.application

class BankAccountNotFoundException(
    bankAccountId: String,
) : RuntimeException("Could not find bank account with id $bankAccountId")
