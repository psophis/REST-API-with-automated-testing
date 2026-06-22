package com.bank.client.api

import com.bank.client.application.ClientService
import com.bank.client.application.CreateClientCommand
import com.bank.client.application.UpdateClientCommand
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant

@RestController
@RequestMapping("/api/clients")
class ClientController(
    private val clientService: ClientService,
) {
    val logger = LoggerFactory.getLogger(ClientController::class.java)

    @GetMapping("/{clientId}")
    fun getClient(
        @PathVariable clientId: String,
    ): ResponseEntity<ClientUpdateRequest> {
        try {
            val client = clientService.getClient(clientId)
            return ResponseEntity.ok(
                ClientUpdateRequest(
                    id = client.id,
                    name = client.name,
                    address = client.address,
                ),
            )
        } catch (e: NoSuchElementException) {
            logger.warn("Client not found: $clientId", e)
            return ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Error getting client: $clientId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{clientId}/accounts")
    fun getClientAccounts(
        @PathVariable clientId: String,
    ): ResponseEntity<List<ClientAccountResponse>> {
        try {
            val accounts = clientService.getClientAccounts(clientId)
            return ResponseEntity.ok(
                accounts.map {
                    ClientAccountResponse(
                        id = it.id,
                        clientId = it.clientId,
                        iban = it.iban,
                        balance = it.balance,
                        createdAt = it.createdAt,
                    )
                },
            )
        } catch (e: NoSuchElementException) {
            logger.warn("Client not found: $clientId", e)
            return ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Error getting accounts for client: $clientId", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping
    fun createClient(
        @RequestBody client: ClientCreationRequest,
    ): ResponseEntity<ClientUpdateRequest> {
        try {
            val createdClient =
                clientService.createClient(
                    CreateClientCommand(
                        name = client.name,
                        address = client.address,
                    ),
                )

            return ResponseEntity.status(HttpStatus.CREATED).body(
                ClientUpdateRequest(
                    id = createdClient.id,
                    name = createdClient.name,
                    address = createdClient.address,
                ),
            )
        } catch (e: Exception) {
            logger.error(e.message, e)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build()
        }
    }

    @PutMapping
    fun updateClient(
        @RequestBody client: ClientUpdateRequest,
    ): ResponseEntity<ClientUpdateRequest> {
        try {
            val updatedClient =
                clientService.updateClient(
                    UpdateClientCommand(
                        id = client.id,
                        name = client.name,
                        address = client.address,
                    ),
                )

            return ResponseEntity.status(HttpStatus.OK).body(
                ClientUpdateRequest(
                    id = updatedClient.id,
                    name = updatedClient.name,
                    address = updatedClient.address,
                ),
            )
        } catch (e: Exception) {
            logger.error(e.message, e)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    @DeleteMapping("/{clientId}")
    fun deleteClient(
        @PathVariable clientId: String,
    ): ResponseEntity<Void> {
        try {
            clientService.deleteClient(clientId)
            return ResponseEntity.noContent().build()
        } catch (e: NoSuchElementException) {
            logger.warn("Client: $clientId not found", e)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
}

data class ClientAccountResponse(
    val id: String,
    val clientId: String,
    val iban: String,
    val balance: BigDecimal,
    val createdAt: Instant,
)
