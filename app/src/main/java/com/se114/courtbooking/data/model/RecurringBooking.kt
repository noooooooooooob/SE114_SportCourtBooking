package com.se114.courtbooking.data.model

import com.google.firebase.Timestamp

data class RecurringBooking(
    var id: String = "",
    val bookingCode: String = "",
    val customerId: String = "",
    val facilityId: String = "",
    val facilityName: String = "",
    val courtId: String = "",
    val courtName: String = "",
    val daysOfWeek: List<String> = emptyList(),
    val startTime: String = "",
    val endTime: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val pricePerSession: Double = 140000.0,
    val totalAmount: Double = 320000.0,
    val status: String = "CONFIRMED",
    val createdAt: Timestamp? = null
)