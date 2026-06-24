package com.bank.bankaccount.api

import com.bank.bankaccount.application.BankAccountNotFoundException
import com.bank.bankaccount.application.BankAccountService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/accounts")
class BankAccountController(
    private val bankAccountService: BankAccountService,
) {
    val logger = LoggerFactory.getLogger(BankAccountController::class.java)

    @GetMapping("/{bankAccountId}")
    fun getBankAccount(
        @PathVariable bankAccountId: String,
    ): ResponseEntity<BankAccountDto> {
        return try {
            val bankAccount = bankAccountService.getBankAccount(bankAccountId)
            ResponseEntity.ok(
                BankAccountDto(
                    id = bankAccount.id,
                    clientId = bankAccount.clientId,
                    iban = bankAccount.iban,
                    balance = bankAccount.balance,
                    createdAt = bankAccount.createdAt,
                ),
            )
        } catch (e: BankAccountNotFoundException) {
            logger.warn("Failed to get account with id {}: {}", bankAccountId, e.message)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Error getting account with id {}: {}", bankAccountId, e.message)
            ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping
    fun createBankAccount(
        @RequestBody request: BankAccountRequest,
    ): ResponseEntity<BankAccountDto> {
        val bankAccount =
            bankAccountService.createBankAccount(
                request.clientId,
            )
        return ResponseEntity.status(HttpStatus.CREATED).body(
            BankAccountDto(
                id = bankAccount.id,
                clientId = bankAccount.clientId,
                iban = bankAccount.iban,
                balance = bankAccount.balance,
                createdAt = bankAccount.createdAt,
            ),
        )
    }

    @DeleteMapping("/{bankAccountId}")
    fun deleteBankAccount(
        @PathVariable bankAccountId: String,
    ): ResponseEntity.HeadersBuilder<*> {
        bankAccountService.deleteBankAccount(bankAccountId)
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
    }
}
