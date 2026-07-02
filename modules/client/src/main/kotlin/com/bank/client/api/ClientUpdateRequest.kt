package com.bank.client.api

data class ClientUpdateRequest(
    val id: String,
    val name: ClientNameDto? = null,
    val address: ClientAddressDto? = null,
)
