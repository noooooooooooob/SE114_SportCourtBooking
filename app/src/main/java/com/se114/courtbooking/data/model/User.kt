package com.se114.courtbooking.data.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: Role = Role.CUSTOMER
)