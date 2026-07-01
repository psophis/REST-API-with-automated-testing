package com.bank.payment.api

import com.bank.payment.application.TransactionService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val transactionService: TransactionService,
) {
    @GetMapping("/{transactionId}")
    fun getTransaction(
        @PathVariable transactionId: String,
    ): ResponseEntity<TransactionDto> {
        val transaction = transactionService.getTransaction(transactionId)
        return ResponseEntity.ok(transaction.toDto())
    }

    @GetMapping("/{accountId}/transactions")
    fun getAccountTransactions(
        @PathVariable accountId: String,
    ): ResponseEntity<List<TransactionDto>> {
        val transactions = transactionService.getAccountTransactions(accountId)
        return ResponseEntity.ok(transactions.map { it.toDto() })
    }
}
