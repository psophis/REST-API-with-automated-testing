package com.bank.payment.persistence

import com.bank.bankaccount.domain.BankAccountTransactionRepository
import com.bank.payment.domain.Transaction
import com.bank.payment.domain.TransactionRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class TransactionRepositoryImpl(
    private val transactionJpaRepository: TransactionJpaRepository,
) : TransactionRepository,
    BankAccountTransactionRepository {
    @Transactional(readOnly = true)
    override fun getTransactionById(id: String): Transaction =
        transactionJpaRepository
            .findById(id)
            .map(TransactionEntity::toDomain)
            .orElseThrow { NoSuchElementException("Transaction not found: $id") }

    @Transactional(readOnly = true)
    override fun getTransactionsByAccountId(accountId: String): List<Transaction> =
        transactionJpaRepository.findAllByAccountId(accountId).map(TransactionEntity::toDomain)

    @Transactional
    override fun createTransaction(transaction: Transaction): Transaction = transactionJpaRepository.save(transaction.toEntity()).toDomain()

    @Transactional
    override fun deleteTransactionsByBankAccountId(bankAccountId: String) {
        transactionJpaRepository.deleteAllByAccountId(bankAccountId)
    }
}

private fun TransactionEntity.toDomain(): Transaction =
    Transaction(
        id = id,
        accountId = accountId,
        senderIban = senderIban,
        recipientIban = recipientIban,
        amount = amount,
        type = type,
        createdAt = createdAt,
    )

private fun Transaction.toEntity(): TransactionEntity =
    TransactionEntity(
        id = id,
        accountId = accountId,
        senderIban = senderIban,
        recipientIban = recipientIban,
        amount = amount,
        type = type,
        createdAt = createdAt,
    )
