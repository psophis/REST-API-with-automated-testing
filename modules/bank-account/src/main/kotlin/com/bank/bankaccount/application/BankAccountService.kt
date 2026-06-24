package com.bank.bankaccount.application

import com.bank.bankaccount.domain.BankAccount
import com.bank.bankaccount.domain.BankAccountRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import javax.security.auth.login.AccountNotFoundException

@Service
class BankAccountService(
    private val bankAccountRepository: BankAccountRepository,
    private val ibanGenerator: IbanGenerator,
) {
    fun getBankAccount(accountId: String): BankAccount {
        try {
            return bankAccountRepository.getBankAccountById(accountId)
                ?: throw BankAccountNotFoundException(accountId)
        } catch (e: Exception) {
            throw e
        }
    }

    fun createBankAccount(clientId: String): BankAccount {
        try {
            val bankAccount =
                BankAccount(
                    id = UUID.randomUUID().toString(),
                    clientId = clientId,
                    iban = ibanGenerator.generateIban(),
                    balance = BigDecimal.ZERO,
                    createdAt = Instant.now(),
                )
            return bankAccountRepository.createBankAccount(bankAccount)
        } catch (e: Exception) {
            throw e
        }
    }

    fun deleteBankAccount(accountId: String) {
        try {
            return bankAccountRepository.deleteByBankAccountId(accountId)
        } catch (e: Exception) {
            throw e
        }
    }
}
