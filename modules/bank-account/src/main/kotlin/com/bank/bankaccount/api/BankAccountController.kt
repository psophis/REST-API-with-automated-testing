package com.bank.bankaccount.api

import com.bank.bankaccount.application.BankAccountService
import com.bank.bankaccount.domain.BankAccount
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
    @GetMapping("/{bankAccountId}")
    fun getBankAccount(
        @PathVariable bankAccountId: String,
    ): ResponseEntity<BankAccountDto> {
        val bankAccount = bankAccountService.getBankAccount(bankAccountId)
        return ResponseEntity.ok(bankAccount.toDto())
    }

    @PostMapping
    fun createBankAccount(
        @RequestBody request: BankAccountRequest,
    ): ResponseEntity<BankAccountDto> {
        val bankAccount =
            bankAccountService.createBankAccount(
                request.clientId,
            )
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(bankAccount.toDto())
    }

    @DeleteMapping("/{bankAccountId}")
    fun deleteBankAccount(
        @PathVariable bankAccountId: String,
    ): ResponseEntity<Void> {
        bankAccountService.deleteBankAccount(bankAccountId)
        return ResponseEntity.noContent().build()
    }

    private fun BankAccount.toDto(): BankAccountDto =
        BankAccountDto(
            id = this.id,
            clientId = this.clientId,
            iban = this.iban,
            balance = this.balance,
            createdAt = this.createdAt,
        )
}
