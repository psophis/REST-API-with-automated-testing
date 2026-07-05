package com.bank.bankaccount.persistence

import com.bank.bankaccount.domain.BankAccount
import com.bank.bankaccount.domain.BankAccountRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Repository
class BankAccountRepositoryImpl(
    private val bankAccountJpaRepository: BankAccountJpaRepository,
) : BankAccountRepository {
    @Transactional(readOnly = true)
    override fun getBankAccountById(bankAccountId: String): BankAccount? =
        bankAccountJpaRepository
            .findById(bankAccountId)
            .map(BankAccountEntity::toDomain)
            .orElse(null)

    @Transactional(readOnly = true)
    override fun getBankAccountByIban(iban: String): BankAccount? = bankAccountJpaRepository.findByIban(iban)?.toDomain()

    @Transactional(readOnly = true)
    override fun getBankAccountsByClientId(clientId: String): List<BankAccount> =
        bankAccountJpaRepository.findAllByClientId(clientId).map(BankAccountEntity::toDomain)

    @Transactional
    override fun createBankAccount(bankAccount: BankAccount): BankAccount = bankAccountJpaRepository.save(bankAccount.toEntity()).toDomain()

    @Transactional
    override fun increaseBankAccountBalance(
        bankAccountId: String,
        amount: BigDecimal,
    ) {
        require(amount > BigDecimal.ZERO) { "Amount must be more than zero" }

        val entity =
            bankAccountJpaRepository
                .findById(bankAccountId)
                .orElseThrow { NoSuchElementException("Account not found: $bankAccountId") }
        entity.balance += amount
        bankAccountJpaRepository.save(entity)
    }

    @Transactional
    override fun decreaseBankAccountBalance(
        bankAccountId: String,
        amount: BigDecimal,
    ) {
        require(amount > BigDecimal.ZERO) { "Amount must be more than zero" }

        val entity =
            bankAccountJpaRepository
                .findById(bankAccountId)
                .orElseThrow { NoSuchElementException("Account not found: $bankAccountId") }
        if (entity.balance >= amount) {
            entity.balance -= amount
        } else {
            throw IllegalStateException("Funds must be at least equal to withdrawal amount")
        }
        bankAccountJpaRepository.save(entity)
    }

    @Transactional
    override fun deleteByBankAccountId(bankAccountId: String) {
        bankAccountJpaRepository.deleteById(bankAccountId)
    }

    @Transactional
    override fun deleteBankAccountsByClientId(clientId: String) {
        bankAccountJpaRepository.findAllByClientId(clientId).forEach {
            bankAccountJpaRepository.delete(it)
        }
    }
}

private fun BankAccountEntity.toDomain(): BankAccount =
    BankAccount(
        id = id,
        clientId = clientId,
        iban = iban,
        balance = balance,
        createdAt = createdAt,
    )

private fun BankAccount.toEntity(): BankAccountEntity =
    BankAccountEntity(
        id = id,
        clientId = clientId,
        iban = iban,
        balance = balance,
        createdAt = createdAt,
    )
