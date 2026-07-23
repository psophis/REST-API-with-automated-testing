package com.bank.payment.persistence

import com.bank.payment.domain.Transaction
import com.bank.payment.domain.TransactionType
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringBootConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ContextConfiguration
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@DataJpaTest
@Import(TransactionRepositoryImpl::class)
@ContextConfiguration(classes = [TransactionRepositoryIntegrationTest.JpaTestConfig::class])
class TransactionRepositoryIntegrationTest {
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan("com.bank.payment.persistence")
    @EnableJpaRepositories("com.bank.payment.persistence")
    class JpaTestConfig

    @Autowired
    private lateinit var transactionRepository: TransactionRepositoryImpl

    @Autowired
    private lateinit var transactionJpaRepository: TransactionJpaRepository

    @Test
    fun `should load transaction by id`() {
        val transaction = transaction()
        transactionRepository.createTransaction(transaction)

        val loaded = transactionRepository.getTransactionById(transaction.id)

        Assertions.assertThat(loaded).isEqualTo(transaction)
    }

    @Test
    fun `should delete transaction by account id`() {
        val transaction = transaction()
        transactionRepository.createTransaction(transaction)

        transactionRepository.deleteTransactionsByBankAccountId(transaction.accountId)

        org.junit.jupiter.api.Assertions.assertThrows(NoSuchElementException::class.java) {
            transactionRepository.getTransactionById(transaction.id)
        }
        Assertions.assertThat(transactionJpaRepository.findById(transaction.id)).isEmpty
    }

    @Test
    fun `should throw when transaction does not exist`() {
        val transactionId = "missing-transaction"

        org.junit.jupiter.api.Assertions.assertThrows(NoSuchElementException::class.java) {
            transactionRepository.getTransactionById(transactionId)
        }
    }

    @Test
    fun `should keep transaction history consistent`() {
        val accountId = UUID.randomUUID().toString()
        val transaction1 = transaction(accountId = accountId, amount = BigDecimal("100.00"), type = TransactionType.WITHDRAWAL)
        val transaction2 = transaction(accountId = accountId, amount = BigDecimal("200.00"), type = TransactionType.DEPOSIT)
        transactionRepository.createTransaction(transaction1)
        transactionRepository.createTransaction(transaction2)

        val transactions = transactionRepository.getTransactionsByAccountId(accountId)

        Assertions.assertThat(transactions).hasSize(2)
        Assertions.assertThat(transactions).containsExactlyInAnyOrder(transaction1, transaction2)
        Assertions.assertThat(transactions.map { it.id }).containsExactly(transaction1.id, transaction2.id)
        Assertions
            .assertThat(transactions.map { it.amount })
            .containsExactly(BigDecimal("100.00"), BigDecimal("200.00"))
        Assertions.assertThat(transactions.map { it.type }).containsExactly(TransactionType.WITHDRAWAL, TransactionType.DEPOSIT)
    }

    private fun transaction(
        id: String = UUID.randomUUID().toString(),
        accountId: String = UUID.randomUUID().toString(),
        senderIban: String = "DE12345678901234567890",
        recipientIban: String = "DE09876543210987654321",
        amount: BigDecimal = BigDecimal("100.00"),
        type: TransactionType = TransactionType.TRANSFER,
        createdAt: Instant = Instant.now(),
    ) = Transaction(
        id = id,
        accountId = accountId,
        senderIban = senderIban,
        recipientIban = recipientIban,
        amount = amount,
        type = type,
        createdAt = createdAt,
    )
}
