package com.bank.payment.domain

import java.math.BigDecimal
import java.time.Instant

/**
 * Represents a financial transaction associated with an account.
 *
 * @property id Unique identifier for the transaction.
 * @property accountId Identifier of the account involved in the transaction.
 * @property amount The amount of money involved in the transaction.
 * @property type The type of transaction (e.g., withdrawal, deposit, transfer).
 */
data class Transaction(
    val id: String,
    val accountId: String,
    val senderIban: String,
    val recipientIban: String,
    val amount: BigDecimal,
    val type: TransactionType,
    val createdAt: Instant,
)
