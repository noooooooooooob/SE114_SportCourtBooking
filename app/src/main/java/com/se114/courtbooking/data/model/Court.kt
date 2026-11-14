package com.se114.courtbooking.data.model

data class Court(
    var id: String = "",
    val facilityId: String = "",
    val name: String = "",
    val courtType: String = "STANDARD_INDOOR",
    val status: String = "ACTIVE"
)