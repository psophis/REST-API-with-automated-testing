package com.bank.client.application

import com.bank.client.domain.ClientAddress
import com.bank.client.domain.ClientName

data class UpdateClientCommand(
    val id: String,
    val name: ClientName,
    val address: ClientAddress,
)
