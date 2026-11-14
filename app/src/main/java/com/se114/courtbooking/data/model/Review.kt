package com.se114.courtbooking.data.model

import com.google.firebase.Timestamp

data class Review(
    var id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val facilityId: String = "",
    val rating: Double = 5.0,
    val comment: String = "",
    val createdAt: Timestamp? = null
)