package com.bank.client.application

import com.bank.bankaccount.application.BankAccountService
import com.bank.bankaccount.domain.BankAccount
import com.bank.bankaccount.domain.BankAccountRepository
import com.bank.client.domain.Client
import com.bank.client.domain.ClientAddress
import com.bank.client.domain.ClientName
import com.bank.client.domain.ClientRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class ClientService(
    private val clientRepository: ClientRepository,
    private val bankAccountRepository: BankAccountRepository,
    private val bankAccountService: BankAccountService,
) {
    fun getClient(clientId: String): Client = clientRepository.getClientById(clientId)

    fun getClientAccounts(clientId: String): List<BankAccount> {
        clientRepository.getClientById(clientId)
        return bankAccountRepository.getBankAccountsByClientId(clientId)
    }

    @Transactional
    fun createClient(command: CreateClientCommand): Client {
        val client =
            Client(
                id = UUID.randomUUID().toString(),
                name =
                    ClientName(
                        name = command.name.name,
                        firstName = command.name.firstName,
                    ),
                address =
                    ClientAddress(
                        street = command.address.street,
                        number = command.address.number,
                        zipCode = command.address.zipCode,
                        city = command.address.city,
                    ),
            )

        val createdClient = clientRepository.createClient(client)
        bankAccountService.createBankAccount(
            clientId = createdClient.id,
        )

        return createdClient
    }

    @Transactional
    fun updateClient(command: UpdateClientCommand): Client {
        val existingClient = clientRepository.getClientById(command.id)

        val updatedClient =
            existingClient.copy(
                name = command.name ?: existingClient.name,
                address = command.address ?: existingClient.address,
            )

        return clientRepository.updateClient(updatedClient)
    }

    @Transactional
    fun deleteClient(clientId: String) {
        clientRepository.deleteClientById(clientId)
        bankAccountRepository.deleteBankAccountByClientId(clientId)
    }
}
