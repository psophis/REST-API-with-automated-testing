package com.bank.bankaccount.api

import com.bank.bankaccount.application.BankAccountNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class BankAccountExceptionHandler {
    @ExceptionHandler(BankAccountNotFoundException::class)
    fun handleBankAccountNotFound(exception: BankAccountNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(exception.message ?: "Bank account not found"),
            )

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(exception: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(exception.message ?: "Bad request"),
            )

    data class ErrorResponse(
        val message: String,
    )
}
