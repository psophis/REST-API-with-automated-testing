package com.bank.client.api

import com.bank.client.domain.ClientAddress
import com.bank.client.domain.ClientName

data class ClientCreationRequest(
    val name: ClientName,
    val address: ClientAddress,
)
