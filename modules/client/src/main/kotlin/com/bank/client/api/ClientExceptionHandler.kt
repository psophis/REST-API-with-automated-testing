package com.bank.client.api

import com.bank.client.application.ClientNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ClientExceptionHandler {
    @ExceptionHandler(ClientNotFoundException::class)
    fun handleClientNotFound(exception: ClientNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(exception.message ?: "Client not found"),
            )

    data class ErrorResponse(
        val message: String,
    )
}
