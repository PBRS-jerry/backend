package com.example.userservice.model

import jakarta.persistence.*

@Entity

@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, unique = true)
    var username: String,

    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    var role: String = "USER",

    @Column(nullable = false)
    var isActive: Boolean = true
)