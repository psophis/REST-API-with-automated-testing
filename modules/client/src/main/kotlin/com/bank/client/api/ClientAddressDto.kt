package com.bank.client.api

data class ClientAddressDto(
    val street: String,
    val number: String,
    val zipCode: String,
    val city: String,
)
