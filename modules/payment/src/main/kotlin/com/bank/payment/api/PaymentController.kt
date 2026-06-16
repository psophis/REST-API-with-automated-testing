package com.bank.payment.api

import com.bank.payment.application.PaymentService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.security.auth.login.AccountNotFoundException

@RestController
@RequestMapping("/api/payments")
class PaymentController(
    private val paymentService: PaymentService,
) {
    val logger = LoggerFactory.getLogger(PaymentController::class.java)

    @PostMapping("/transfer")
    fun sendBankTransfer(
        @RequestBody request: BankTransferRequest,
    ): ResponseEntity<TransactionDto> {
        try {
            val transaction =
                paymentService.transferMoney(
                    fromIban = request.senderIban,
                    toIban = request.recipientIban,
                    amount = request.amount,
                )
            return ResponseEntity.ok(
                TransactionDto(
                    id = transaction.id,
                    bankAccountId = transaction.accountId,
                    amount = transaction.amount,
                    type = transaction.type,
                    createdAt = transaction.createdAt,
                ),
            )
        } catch (e: AccountNotFoundException) {
            logger.error("Sender or recipient bank account not found", e)
            return ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Error sending bank transfer", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/withdrawal")
    fun withdrawMoney(
        @RequestBody request: WithdrawalRequest,
    ): ResponseEntity<Void> {
        try {
            paymentService.withdrawMoney(
                accountId = request.bankAccountId,
                amount = request.amount,
            )
            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            logger.error("Error withdrawing money", e)
            return ResponseEntity.internalServerError().build()
        }
    }

    @PostMapping("/deposit")
    fun depositMoney(
        @RequestBody request: DepositRequest,
    ): ResponseEntity<Void> {
        try {
            paymentService.depositMoney(
                accountId = request.bankAccountId,
                amount = request.amount,
            )
            return ResponseEntity.ok().build()
        } catch (e: Exception) {
            logger.error("Error depositing money", e)
            return ResponseEntity.internalServerError().build()
        }
    }
}
