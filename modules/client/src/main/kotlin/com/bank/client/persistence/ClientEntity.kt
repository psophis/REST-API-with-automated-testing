package com.bank.client.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "clients")
class ClientEntity(
    @Id
    @Column(nullable = false, length = 36)
    var id: String,
    @Column(name = "last_name", nullable = false, length = 50)
    var lastName: String,
    @Column(name = "first_name", nullable = false, length = 50)
    var firstName: String,
    @Column(name = "street", nullable = false, length = 60)
    var street: String,
    @Column(name = "number", nullable = false, length = 12)
    var number: String,
    @Column(name = "city", nullable = false, length = 60)
    var city: String,
    @Column(name = "zip_code", nullable = false, length = 12)
    var zipCode: String,
)
