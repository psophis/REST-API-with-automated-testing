package contracts

import org.springframework.cloud.contract.spec.ContractDsl.Companion.contract

contract {
    name = "should_create_bank_account"

    request {
        method = POST
        url = url("/api/accounts")
        headers {
            header("Content-Type", "application/json")
        }
        body = body(
            mapOf(
                "clientId" to "client-id",
            ),
        )
    }

    response {
        status = CREATED
        headers {
            header("Content-Type", "application/json")
        }
        body = body(
            mapOf(
                "id" to "account-id",
                "clientId" to "client-id",
                "iban" to "DE02100100100006820101",
                "balance" to 0,
                "createdAt" to "2026-07-15T10:00:00Z",
            ),
        )
        bodyMatchers {
            jsonPath("$.id", byRegex(".+"))
            jsonPath("$.clientId", byRegex(".+"))
            jsonPath("$.iban", byRegex("DE[0-9]{20}"))
            jsonPath("$.createdAt", byRegex("[0-9]{4}-[0-9]{2}-[0-9]{2}T.*Z"))
        }
    }
}
