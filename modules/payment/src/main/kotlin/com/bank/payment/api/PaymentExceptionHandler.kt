package com.bank.payment.api

import com.bank.payment.application.PaymentAccountNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice(assignableTypes = [PaymentController::class, TransactionController::class])
class PaymentExceptionHandler {
    @ExceptionHandler(PaymentAccountNotFoundException::class)
    fun handleAccountNotFound(exception: PaymentAccountNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(exception.message ?: "Account not found"))

    @ExceptionHandler(Exception::class)
    fun handleException(exception: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(exception.message ?: "Internal server error"))

    data class ErrorResponse(
        val message: String,
    )
}
