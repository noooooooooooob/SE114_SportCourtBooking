package com.se114.courtbooking.data.model

data class Facility(
    var id: String = "",
    val name: String = "",
    val address: String = "",
    val district: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val openTime: String = "",
    val closeTime: String = "",
    val minPrice: Double? = null,
    val ratingAvg: Double = 0.0,
    val ratingCount: Int = 0,
    val description: String = "",
    val images: List<String> = emptyList(),
    val amenities: List<String> = emptyList(),
    val status: String = "ACTIVE"
)
