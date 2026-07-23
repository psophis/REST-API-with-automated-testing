package com.bank.client.api

import com.bank.client.application.ClientNotFoundException
import com.bank.client.application.ClientService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.module.kotlin.jacksonMapperBuilder

class ClientExceptionHandlerTest {
    private val clientService = mockk<ClientService>()

    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(ClientController(clientService))
            .setControllerAdvice(ClientExceptionHandler())
            .setMessageConverters(JacksonJsonHttpMessageConverter(jacksonMapperBuilder()))
            .build()

    @Test
    fun `should return 404 for ClientNotFoundException`() {
        val clientId = "missing-client"

        every { clientService.getClient(clientId) } throws ClientNotFoundException(clientId)

        mockMvc
            .get("/api/clients/{clientId}", clientId)
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("Could not find client with id $clientId") }
            }
    }
}
