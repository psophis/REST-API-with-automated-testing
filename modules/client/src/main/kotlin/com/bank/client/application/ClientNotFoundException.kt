package com.bank.client.application

class ClientNotFoundException(
    clientId: String,
) : RuntimeException("Could not find client with id $clientId")