package com.bank.client.api

import com.bank.bankaccount.domain.BankAccount
import com.bank.client.application.CreateClientCommand
import com.bank.client.application.UpdateClientCommand
import com.bank.client.domain.Client
import com.bank.client.domain.ClientAddress
import com.bank.client.domain.ClientName

fun Client.toDto(): ClientUpdateRequest =
    ClientUpdateRequest(
        id = id,
        name = name.toDto(),
        address = address.toDto(),
    )

fun BankAccount.toDto(): ClientAccountResponse =
    ClientAccountResponse(
        id = id,
        clientId = clientId,
        iban = iban,
        balance = balance,
        createdAt = createdAt,
    )

fun ClientCreationRequest.toCommand(): CreateClientCommand =
    CreateClientCommand(
        name = name.toDomain(),
        address = address.toDomain(),
    )

fun ClientUpdateRequest.toCommand(): UpdateClientCommand =
    UpdateClientCommand(
        id = id,
        name = name?.toDomain(),
        address = address?.toDomain(),
    )

private fun ClientName.toDto(): ClientNameDto =
    ClientNameDto(
        name = name,
        firstName = firstName,
    )

private fun ClientAddress.toDto(): ClientAddressDto =
    ClientAddressDto(
        street = street,
        number = number,
        zipCode = zipCode,
        city = city,
    )

private fun ClientNameDto.toDomain(): ClientName =
    ClientName(
        name = name,
        firstName = firstName,
    )

private fun ClientAddressDto.toDomain(): ClientAddress =
    ClientAddress(
        street = street,
        number = number,
        zipCode = zipCode,
        city = city,
    )
