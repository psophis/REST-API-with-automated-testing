package com.bank.bankaccount.application

class BankAccountHasNonZeroBalanceException(
    bankAccountId: String,
) : RuntimeException("Bank account with id $bankAccountId has non-zero balance and cannot be deleted")
