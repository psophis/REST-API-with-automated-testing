package com.bank.client.persistence

import com.bank.client.domain.Client
import com.bank.client.domain.ClientAddress
import com.bank.client.domain.ClientName
import com.bank.client.domain.ClientRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class ClientRepositoryImpl(
    private val clientJpaRepository: ClientJpaRepository,
) : ClientRepository {
    @Transactional(readOnly = true)
    override fun getClientById(clientId: String): Client? =
        clientJpaRepository
            .findById(clientId)
            .map(ClientEntity::toDomain)
            .orElse(null)

    override fun createClient(client: Client): Client = clientJpaRepository.save(client.toEntity()).toDomain()

    @Transactional
    override fun updateClient(client: Client): Client {
        if (!clientJpaRepository.existsById(client.id)) {
            throw NoSuchElementException("Client not found: ${client.id}")
        }
        return clientJpaRepository.save(client.toEntity()).toDomain()
    }

    @Transactional
    override fun deleteClientById(clientId: String) {
        if (!clientJpaRepository.existsById(clientId)) {
            throw NoSuchElementException("Client not found: $clientId")
        }
        clientJpaRepository.deleteById(clientId)
    }
}

private fun ClientEntity.toDomain(): Client =
    Client(
        id = id,
        name =
            ClientName(
                name = lastName,
                firstName = firstName,
            ),
        address =
            ClientAddress(
                street = street,
                number = number,
                zipCode = zipCode,
                city = city,
            ),
    )

private fun Client.toEntity(): ClientEntity =
    ClientEntity(
        id = id,
        lastName = name.name,
        firstName = name.firstName,
        street = address.street,
        number = address.number,
        city = address.city,
        zipCode = address.zipCode,
    )
