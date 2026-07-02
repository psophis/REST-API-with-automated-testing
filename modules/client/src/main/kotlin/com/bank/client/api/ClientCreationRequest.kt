package com.bank.client.api

data class ClientCreationRequest(
    val name: ClientNameDto,
    val address: ClientAddressDto,
)
