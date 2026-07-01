package com.bank.payment.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.security.auth.login.AccountNotFoundException

@RestControllerAdvice(assignableTypes = [PaymentController::class, TransactionController::class])
class PaymentExceptionHandler {
    @ExceptionHandler(AccountNotFoundException::class)
    fun handleAccountNotFound(
        exception: AccountNotFoundException,
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(exception.message ?: "Account not found"))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(
        exception: Exception,
    ): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(exception.message ?: "Internal server error"))
    }

    data class ErrorResponse(
        val message: String,
    )
}
