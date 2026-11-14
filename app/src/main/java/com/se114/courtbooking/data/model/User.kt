package com.se114.courtbooking.data.model

import com.se114.courtbooking.data.model.enums.Role

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: Role = Role.CUSTOMER
)