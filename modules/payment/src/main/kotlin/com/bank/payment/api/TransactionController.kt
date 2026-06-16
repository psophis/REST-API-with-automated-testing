package com.bank.payment.api

import com.bank.payment.application.TransactionService
import org.slf4j.LoggerFactory
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
    val logger = LoggerFactory.getLogger(TransactionController::class.java)

    @GetMapping("/{transactionId}")
    fun getTransaction(
        @PathVariable transactionId: String,
    ): ResponseEntity<TransactionDto> {
        try {
            val transaction = transactionService.getTransaction(transactionId)
            return ResponseEntity.ok(
                TransactionDto(
                    id = transaction.id,
                    bankAccountId = transaction.accountId,
                    amount = transaction.amount,
                    type = transaction.type,
                    createdAt = transaction.createdAt,
                ),
            )
        } catch (e: Exception) {
            logger.warn(e.message, e)
            return ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/{accountId}/transactions")
    fun getAccountTransactions(
        @PathVariable accountId: String,
    ): ResponseEntity<List<TransactionDto>> {
        try {
            val transactions = transactionService.getAccountTransactions(accountId)
            return ResponseEntity.ok(
                transactions.map { transaction ->
                    TransactionDto(
                        id = transaction.id,
                        bankAccountId = transaction.accountId,
                        amount = transaction.amount,
                        type = transaction.type,
                        createdAt = transaction.createdAt,
                    )
                },
            )
        } catch (e: Exception) {
            logger.error(e.message, e)
            return ResponseEntity.internalServerError().build()
        }
    }
}
