package com.bank.client.application

class ClientHasNonZeroBalanceException(
    clientId: String
) : RuntimeException("Could not delete client with id $clientId because at least one bank account has a non-zero balance")