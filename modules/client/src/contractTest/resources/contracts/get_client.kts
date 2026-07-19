package contracts

import org.springframework.cloud.contract.spec.ContractDsl.Companion.contract

contract {
    name = "should_get_client"

    request {
        method = GET
        url = url("/api/clients/client-id")
    }

    response {
        status = OK
        headers {
            header("Content-Type", "application/json")
        }
        body = body(
            mapOf(
                "id" to "client-id",
                "name" to
                    mapOf(
                        "name" to "Smith",
                        "firstName" to "Jane",
                    ),
                "address" to
                    mapOf(
                        "street" to "Elm Street",
                        "number" to "456",
                        "zipCode" to "12345",
                        "city" to "Berlin",
                    ),
            ),
        )
        bodyMatchers {
            jsonPath("$.id", byRegex(".+"))
            jsonPath("$.name.name", byRegex(".+"))
            jsonPath("$.name.firstName", byRegex(".+"))
            jsonPath("$.address.street", byRegex(".+"))
            jsonPath("$.address.number", byRegex(".+"))
            jsonPath("$.address.zipCode", byRegex("[0-9]{5}"))
            jsonPath("$.address.city", byRegex(".+"))
        }
    }
}
