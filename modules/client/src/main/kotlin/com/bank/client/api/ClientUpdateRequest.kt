package com.bank.client.api

import com.bank.client.domain.ClientAddress
import com.bank.client.domain.ClientName

data class ClientUpdateRequest(
    val id: String,
    val name: ClientName,
    val address: ClientAddress,
)
