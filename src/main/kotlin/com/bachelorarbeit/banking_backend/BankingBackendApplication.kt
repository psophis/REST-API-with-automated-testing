package com.bachelorarbeit.banking_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BankingBackendApplication

fun main(args: Array<String>) {
	runApplication<BankingBackendApplication>(*args)
}
