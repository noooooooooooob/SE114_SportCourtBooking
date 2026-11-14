package com.se114.courtbooking.data.model

data class Facility(
    var id: String = "",
    val name: String = "",
    val address: String = "",
    val district: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val openTime: String = "06:00",
    val closeTime: String = "23:00",
    val minPrice: Double = 80000.0,
    val ratingAvg: Double = 4.8,
    val ratingCount: Int = 1200,
    val description: String = "",
    val images: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val status: String = "ACTIVE"
)