package com.bank.bankaccount.application

import org.springframework.stereotype.Component
import java.util.UUID

interface IbanGenerator {
    fun generateIban(): String
}

@Component
class DemoIbanGenerator : IbanGenerator {
    override fun generateIban(): String {
        val bankCode = "10000000"
        val accountNumber =
            UUID
                .randomUUID()
                .toString()
                .replace("-", "")
                .takeLast(10)

        return "DE00$bankCode$accountNumber"
    }
}
