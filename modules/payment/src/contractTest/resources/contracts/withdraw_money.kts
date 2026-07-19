package contracts

import org.springframework.cloud.contract.spec.ContractDsl.Companion.contract

contract {
    name = "should_withdraw_money"

    request {
        method = POST
        url = url("/api/payments/withdrawal")
        headers {
            header("Content-Type", "application/json")
        }
        body = body(
            mapOf(
                "bankAccountId" to "account-id",
                "amount" to 100.00,
            ),
        )
    }

    response {
        status = OK
    }
}
