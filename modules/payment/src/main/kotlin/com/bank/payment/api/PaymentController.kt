package com.bank.payment.api

import com.bank.payment.application.PaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService,
) {
    @PostMapping("/transfer")
    fun sendBankTransfer(
        @RequestBody request: BankTransferRequest,
    ): ResponseEntity<TransactionDto> {
        val transaction =
            paymentService.transferMoney(
                fromIban = request.senderIban,
                toIban = request.recipientIban,
                amount = request.amount,
            )

        return ResponseEntity.ok(transaction.toDto())
    }

    @PostMapping("/withdrawal")
    fun withdrawMoney(
        @RequestBody request: WithdrawalRequest,
    ): ResponseEntity<Void> {
        paymentService.withdrawMoney(
            accountId = request.bankAccountId,
            amount = request.amount,
        )

        return ResponseEntity.ok().build()
    }

    @PostMapping("/deposit")
    fun depositMoney(
        @RequestBody request: DepositRequest,
    ): ResponseEntity<Void> {
        paymentService.depositMoney(
            accountId = request.bankAccountId,
            amount = request.amount,
        )

        return ResponseEntity.ok().build()
    }
}
