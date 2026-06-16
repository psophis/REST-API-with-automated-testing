package com.bank.client.domain

data class Client(
    val id: String,
    val name: ClientName,
    val address: ClientAddress,
)
