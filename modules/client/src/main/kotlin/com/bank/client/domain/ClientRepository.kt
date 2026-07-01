package com.bank.client.domain

interface ClientRepository {
    fun getClientById(clientId: String): Client?

    fun createClient(client: Client): Client

    fun updateClient(client: Client): Client

    fun deleteClientById(clientId: String)
}
