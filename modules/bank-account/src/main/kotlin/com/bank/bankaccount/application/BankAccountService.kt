package com.bank.bankaccount.application

import com.bank.bankaccount.domain.BankAccount
import com.bank.bankaccount.domain.BankAccountRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Service
class BankAccountService(
    private val bankAccountRepository: BankAccountRepository,
    private val ibanGenerator: IbanGenerator,
) {
    fun getBankAccount(bankAccountId: String): BankAccount =
        bankAccountRepository.getBankAccountById(bankAccountId)
            ?: throw BankAccountNotFoundException(bankAccountId)

    fun createBankAccount(clientId: String): BankAccount {
        require(clientId.isNotBlank()) { "ClientId cannot be blank" }

        val bankAccount =
            BankAccount(
                id = UUID.randomUUID().toString(),
                clientId = clientId,
                iban = ibanGenerator.generateIban(),
                balance = BigDecimal.ZERO,
                createdAt = Instant.now(),
            )

        return bankAccountRepository.createBankAccount(bankAccount)
    }

    fun deleteBankAccount(bankAccountId: String) {
        require(bankAccountId.isNotBlank()) { "BankAccountId cannot be blank" }

        bankAccountRepository.deleteByBankAccountId(bankAccountId)
    }
}
