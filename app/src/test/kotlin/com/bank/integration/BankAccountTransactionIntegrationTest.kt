package com.bank.integration

import com.bank.BankingBackendApplication
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(classes = [BankingBackendApplication::class])
@Testcontainers
class BankAccountTransactionIntegrationTest {
    @Test
    fun `should delete transactions when deleting account`() {

    }
}
