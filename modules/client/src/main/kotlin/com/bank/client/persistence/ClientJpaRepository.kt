package com.bank.client.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface ClientJpaRepository : JpaRepository<ClientEntity, String>
