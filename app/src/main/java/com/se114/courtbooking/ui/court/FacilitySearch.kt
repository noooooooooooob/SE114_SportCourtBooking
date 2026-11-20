package com.se114.courtbooking.ui.court

import com.se114.courtbooking.data.model.Facility
import java.text.Collator
import java.text.Normalizer
import java.util.Locale
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal val vietnameseLocale = Locale.forLanguageTag("vi-VN")

internal enum class FacilitySort { NAME, DISTANCE, PRICE, RATING }

internal data class SearchLocation(val latitude: Double, val longitude: Double)

internal data class FacilityResult(val facility: Facility, val distanceKm: Double?)

internal fun searchFacilities(
    facilities: List<Facility>,
    keyword: String,
    district: String?,
    maxPrice: Long?,
    minRating: Double,
    sort: FacilitySort,
    location: SearchLocation?,
    availableIds: Set<String>?
): List<FacilityResult> {
    val terms = normalize(keyword).trim().split(Regex("\\s+")).filter(String::isNotEmpty)
    val byName = compareBy<FacilityResult, String>(
        Collator.getInstance(vietnameseLocale)
    ) { it.facility.name }.thenBy { it.facility.id }
    val order = when (sort) {
        FacilitySort.NAME -> byName
        FacilitySort.DISTANCE -> compareBy<FacilityResult> { it.distanceKm ?: Double.POSITIVE_INFINITY }
        FacilitySort.PRICE -> compareBy<FacilityResult> { it.facility.minPrice ?: Double.POSITIVE_INFINITY }
        FacilitySort.RATING -> compareByDescending<FacilityResult> { it.facility.ratingAvg }
            .thenByDescending { it.facility.ratingCount }
    }

    return facilities.asSequence()
        .filter { availableIds == null || it.id in availableIds }
        .filter { district == null || it.district == district }
        .filter { maxPrice == null || (it.minPrice != null && it.minPrice <= maxPrice) }
        .filter { minRating == 0.0 || (it.ratingCount > 0 && it.ratingAvg >= minRating) }
        .filter { facility ->
            val name = normalize(facility.name)
            terms.all { it in name }
        }
        .map { FacilityResult(it, location?.let { origin -> distanceKm(origin, it) }) }
        .sortedWith(order.then(byName))
        .toList()
}

private val diacritics = Regex("\\p{M}+")

private fun normalize(text: String): String =
    Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(diacritics, "")
        .replace('đ', 'd')

private fun distanceKm(origin: SearchLocation, facility: Facility): Double? {
    if (facility.latitude == 0.0 && facility.longitude == 0.0) return null
    if (facility.latitude !in -90.0..90.0 || facility.longitude !in -180.0..180.0) return null
    val fromLatitude = Math.toRadians(origin.latitude)
    val toLatitude = Math.toRadians(facility.latitude)
    val deltaLongitude = Math.toRadians(facility.longitude - origin.longitude)
    val a = sin((toLatitude - fromLatitude) / 2).pow(2) +
        cos(fromLatitude) * cos(toLatitude) * sin(deltaLongitude / 2).pow(2)
    return 6371.0 * 2 * asin(sqrt(a.coerceIn(0.0, 1.0)))
}
