package com.bank.payment.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface TransactionJpaRepository : JpaRepository<TransactionEntity, String> {
    fun findAllByAccountId(accountId: String): List<TransactionEntity>
}
