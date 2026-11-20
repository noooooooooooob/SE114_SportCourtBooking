package com.se114.courtbooking.ui.court

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.se114.courtbooking.R
import com.se114.courtbooking.data.model.Facility
import com.se114.courtbooking.databinding.ItemCourtFacilityBinding
import java.text.NumberFormat

internal class FacilityAdapter(
    private val onFacilityClicked: (Facility) -> Unit
) : ListAdapter<FacilityResult, FacilityAdapter.FacilityHolder>(Diff) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FacilityHolder {
        val binding = ItemCourtFacilityBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FacilityHolder(binding)
    }

    override fun onBindViewHolder(holder: FacilityHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FacilityHolder(private val binding: ItemCourtFacilityBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(result: FacilityResult) {
            val facility = result.facility
            val context = binding.root.context
            binding.courtName.text = facility.name
            binding.courtAddress.text = result.distanceKm?.let { distance ->
                context.getString(R.string.court_search_distance_address, oneDecimal(distance), facility.address)
            } ?: facility.address
            binding.courtPrice.text = context.priceText(facility.minPrice)
            binding.courtRating.text = context.ratingText(facility, compact = true)
            binding.courtRating.contentDescription = context.ratingText(facility)
            binding.root.setOnClickListener { onFacilityClicked(facility) }

            val imageUrl = facility.images.firstOrNull { it.startsWith("https://", ignoreCase = true) }
            Glide.with(binding.courtPhoto)
                .load(imageUrl)
                .placeholder(R.drawable.court_photo_placeholder)
                .fallback(R.drawable.court_photo_placeholder)
                .error(R.drawable.court_photo_placeholder)
                .into(binding.courtPhoto)
        }
    }

    private object Diff : DiffUtil.ItemCallback<FacilityResult>() {
        override fun areItemsTheSame(old: FacilityResult, new: FacilityResult): Boolean =
            old.facility.id == new.facility.id

        override fun areContentsTheSame(old: FacilityResult, new: FacilityResult): Boolean = old == new
    }
}

internal fun oneDecimal(number: Double): String =
    NumberFormat.getNumberInstance(vietnameseLocale).apply {
        maximumFractionDigits = 1
    }.format(number)

internal fun Context.priceText(price: Double?): String =
    if (price == null) getString(R.string.court_search_price_unknown)
    else getString(R.string.court_search_price, NumberFormat.getNumberInstance(vietnameseLocale).format(price))

internal fun Context.ratingText(facility: Facility, compact: Boolean = false): String {
    if (facility.ratingCount <= 0) return getString(R.string.court_search_no_ratings)
    val count = if (compact && facility.ratingCount >= 1000) {
        "${oneDecimal(facility.ratingCount / 1000.0)}k"
    } else {
        NumberFormat.getIntegerInstance(vietnameseLocale).format(facility.ratingCount)
    }
    val format = if (compact) R.string.court_search_rating_short else R.string.court_search_rating
    return getString(format, oneDecimal(facility.ratingAvg), count)
}
