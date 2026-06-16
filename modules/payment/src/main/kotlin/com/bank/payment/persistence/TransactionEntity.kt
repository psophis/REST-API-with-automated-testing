package com.bank.payment.persistence

import com.bank.payment.domain.TransactionType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "transactions")
class TransactionEntity(
    @Id
    @Column(nullable = false, length = 36)
    var id: String,
    @Column(name = "account_id", nullable = false, length = 36)
    var accountId: String,
    @Column(name = "sender_iban", nullable = false, length = 34)
    var senderIban: String,
    @Column(name = "recipient_iban", nullable = false, length = 34)
    var recipientIban: String,
    @Column(nullable = false, precision = 19, scale = 2)
    var amount: BigDecimal,
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 32)
    var type: TransactionType,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
)
