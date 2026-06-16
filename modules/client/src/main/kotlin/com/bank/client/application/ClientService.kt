package com.bank.client.application

import com.bank.bankaccount.application.BankAccountService
import com.bank.bankaccount.domain.BankAccount
import com.bank.bankaccount.domain.BankAccountRepository
import com.bank.bankaccount.domain.BankAccountType
import com.bank.client.api.ClientCreationRequest
import com.bank.client.api.ClientUpdateRequest
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
    fun createClient(clientRequest: ClientCreationRequest): Client {
        val client =
            Client(
                id = UUID.randomUUID().toString(),
                name =
                    ClientName(
                        name = clientRequest.name.name,
                        firstName = clientRequest.name.firstName,
                    ),
                address =
                    ClientAddress(
                        street = clientRequest.address.street,
                        number = clientRequest.address.number,
                        zipCode = clientRequest.address.zipCode,
                        city = clientRequest.address.city,
                    ),
            )

        val createdClient = clientRepository.createClient(client)
        bankAccountService.createBankAccount(
            clientId = createdClient.id,
            bankAccountType = BankAccountType.CHECKING_ACCOUNT,
        )

        return createdClient
    }

    @Transactional
    fun updateClient(clientUpdateRequest: ClientUpdateRequest): Client {
        val client =
            Client(
                id = clientUpdateRequest.id,
                name =
                    ClientName(
                        name = clientUpdateRequest.name.name,
                        firstName = clientUpdateRequest.name.firstName,
                    ),
                address =
                    ClientAddress(
                        street = clientUpdateRequest.address.street,
                        number = clientUpdateRequest.address.number,
                        zipCode = clientUpdateRequest.address.zipCode,
                        city = clientUpdateRequest.address.city,
                    ),
            )
        return clientRepository.updateClient(client)
    }

    @Transactional
    fun deleteClient(clientId: String) {
        clientRepository.deleteClientById(clientId)
        bankAccountRepository.deleteBankAccountByClientId(clientId)
    }
}
