package com.bank.client.api

import com.bank.client.application.ClientService
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
    @GetMapping("/{clientId}")
    fun getClient(
        @PathVariable clientId: String,
    ): ResponseEntity<ClientUpdateRequest> {
        val client = clientService.getClient(clientId)
        return ResponseEntity.ok(client.toDto())
    }

    @GetMapping("/{clientId}/accounts")
    fun getClientAccounts(
        @PathVariable clientId: String,
    ): ResponseEntity<List<ClientAccountResponse>> {
        val accounts = clientService.getClientAccounts(clientId)
        return ResponseEntity.ok(accounts.map { it.toDto() })
    }

    @PostMapping
    fun createClient(
        @RequestBody client: ClientCreationRequest,
    ): ResponseEntity<ClientUpdateRequest> {
        val createdClient =
            clientService.createClient(
                client.toCommand(),
            )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdClient.toDto())
    }

    @PutMapping
    fun updateClient(
        @RequestBody client: ClientUpdateRequest,
    ): ResponseEntity<ClientUpdateRequest> {
        val updatedClient =
            clientService.updateClient(
                client.toCommand(),
            )

        return ResponseEntity.ok(updatedClient.toDto())
    }

    @DeleteMapping("/{clientId}")
    fun deleteClient(
        @PathVariable clientId: String,
    ): ResponseEntity<Void> {
        clientService.deleteClient(clientId)
        return ResponseEntity.noContent().build()
    }
}

data class ClientAccountResponse(
    val id: String,
    val clientId: String,
    val iban: String,
    val balance: BigDecimal,
    val createdAt: Instant,
)
