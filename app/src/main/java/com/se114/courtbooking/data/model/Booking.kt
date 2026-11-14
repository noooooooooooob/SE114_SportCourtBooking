package com.se114.courtbooking.data.model

import com.google.firebase.Timestamp

data class Booking(
    var id: String = "",
    val bookingCode: String = "",
    val customerId: String = "",
    val facilityId: String = "",
    val facilityName: String = "",
    val courtId: String = "",
    val courtName: String = "",
    val bookingDate: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val durationHours: Double = 2.0,
    val courtPrice: Double = 80000.0,
    val totalAmount: Double = 160000.0,
    val status: String = "CONFIRMED",
    val paymentStatus: String = "UNPAID",
    val recurringBookingId: String? = null,
    val createdAt: Timestamp? = null
)