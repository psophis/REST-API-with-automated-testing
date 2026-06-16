package com.bank.bankaccount.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface BankAccountJpaRepository : JpaRepository<BankAccountEntity, String> {
    fun findByIban(iban: String): BankAccountEntity?

    fun findAllByClientId(clientId: String): List<BankAccountEntity>
}
