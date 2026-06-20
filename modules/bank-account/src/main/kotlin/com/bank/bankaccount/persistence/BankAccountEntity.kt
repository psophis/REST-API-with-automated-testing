package com.bank.bankaccount.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(name = "accounts")
class BankAccountEntity(
    @Id
    @Column(nullable = false, length = 36)
    var id: String,
    @Column(name = "client_id", nullable = false, length = 36)
    var clientId: String,
    @Column(nullable = false, length = 34, unique = true)
    var iban: String,
    @Column(nullable = false, precision = 19, scale = 2)
    var balance: BigDecimal,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
)
