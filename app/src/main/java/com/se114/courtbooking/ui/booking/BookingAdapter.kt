package com.se114.courtbooking.ui.booking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.se114.courtbooking.R
import com.se114.courtbooking.data.model.Booking
import com.se114.courtbooking.databinding.ItemBookingCardBinding
import java.text.DecimalFormat

class BookingAdapter(
    private var bookings: List<Booking>,
    private val onItemClick: (Booking) -> Unit
) : RecyclerView.Adapter<BookingAdapter.BookingViewHolder>() {

    inner class BookingViewHolder(val binding: ItemBookingCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        val item = bookings[position]
        val context = holder.itemView.context
        val formatter = DecimalFormat("#,###")

        with(holder.binding) {
            tvFacilityName.text = item.facilityName.ifEmpty { "SmashBadminton Center" }
            tvBookingType.text = if (item.recurringBookingId.isNullOrEmpty()) "Đặt sân thông thường" else "Đặt lịch cố định"
            tvBookingDate.text = item.bookingDate
            tvBookingTime.text = "${item.startTime} - ${item.endTime}"
            tvTotalPrice.text = "${formatter.format(item.totalAmount)}đ"

            // Đổi màu badge theo trạng thái
            when (item.status) {
                "CONFIRMED" -> {
                    tvStatusBadge.text = "Đã xác nhận"
                    tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.smash_primary))
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_confirmed)
                }
                "PENDING" -> {
                    tvStatusBadge.text = "Chờ xác nhận"
                    tvStatusBadge.setTextColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
                }
                "CANCELLED" -> {
                    tvStatusBadge.text = "Đã hủy"
                    tvStatusBadge.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_light))
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_pending)
                }
                else -> {
                    tvStatusBadge.text = "Đã hoàn thành"
                    tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.smash_text_sub))
                    tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_confirmed)
                }
            }

            // Click nút hoặc click cả thẻ đều mở chi tiết
            btnViewDetail.setOnClickListener { onItemClick(item) }
            root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount(): Int = bookings.size

    fun updateList(newList: List<Booking>) {
        bookings = newList
        notifyDataSetChanged()
    }
}