package com.bank.client.api

import com.bank.client.application.ClientService
import com.bank.client.domain.Client
import com.bank.client.domain.ClientAddress
import com.bank.client.domain.ClientName
import io.mockk.every
import io.mockk.mockk
import io.restassured.module.mockmvc.RestAssuredMockMvc
import org.junit.jupiter.api.BeforeEach
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.module.kotlin.jacksonMapperBuilder

abstract class ClientContractBase {
    lateinit var mockMvc: MockMvc

    private val clientService = mockk<ClientService>()

    @BeforeEach
    fun setup() {
        every { clientService.getClient("client-id") } returns
            Client(
                id = "client-id",
                name =
                    ClientName(
                        name = "Smith",
                        firstName = "Jane",
                    ),
                address =
                    ClientAddress(
                        street = "Elm Street",
                        number = "456",
                        zipCode = "12345",
                        city = "Berlin",
                    ),
            )

        mockMvc =
            MockMvcBuilders
                .standaloneSetup(ClientController(clientService))
                .setControllerAdvice(ClientExceptionHandler())
                .setMessageConverters(JacksonJsonHttpMessageConverter(jacksonMapperBuilder()))
                .build()

        RestAssuredMockMvc.mockMvc(mockMvc)
    }
}
