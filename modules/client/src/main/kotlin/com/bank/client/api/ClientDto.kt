package com.bank.client.api

data class ClientDto(
    val id: String,
    val name: ClientNameDto,
    val address: ClientAddressDto,
)

data class ClientNameDto(
    val name: String,
    val firstName: String,
)

data class ClientAddressDto(
    val street: String,
    val number: String,
    val zipCode: String,
    val city: String,
)