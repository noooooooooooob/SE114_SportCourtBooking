package com.se114.courtbooking.ui.booking

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.se114.courtbooking.R
import com.se114.courtbooking.data.model.Booking
import com.se114.courtbooking.databinding.ActivityMyBookingsBinding

class MyBookingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyBookingsBinding
    private lateinit var adapter: BookingAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var allBookings = listOf<Booking>()
    private var currentTab = "UPCOMING" // UPCOMING, COMPLETED, CANCELLED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBookingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nút quay lại (<)
        binding.btnBack.setOnClickListener { finish() }

        setupRecyclerView()
        setupTabs()
        loadBookingsFromFirestore()
    }

    private fun setupRecyclerView() {
        adapter = BookingAdapter(emptyList()) { booking ->
            Toast.makeText(this, "Xem chi tiết mã: ${booking.bookingCode}", Toast.LENGTH_SHORT).show()
        }
        binding.rvBookings.layoutManager = LinearLayoutManager(this)
        binding.rvBookings.adapter = adapter
    }

    private fun setupTabs() {
        binding.tabUpcoming.setOnClickListener {
            currentTab = "UPCOMING"
            updateTabUI(binding.tabUpcoming)
            filterBookings()
        }
        binding.tabCompleted.setOnClickListener {
            currentTab = "COMPLETED"
            updateTabUI(binding.tabCompleted)
            filterBookings()
        }
        binding.tabCancelled.setOnClickListener {
            currentTab = "CANCELLED"
            updateTabUI(binding.tabCancelled)
            filterBookings()
        }
    }

    private fun updateTabUI(selectedButton: MaterialButton) {
        val activeColor = ContextCompat.getColor(this, R.color.smash_primary)
        val inactiveColor = ContextCompat.getColor(this, R.color.smash_text_sub)
        val white = ContextCompat.getColor(this, R.color.white)

        val buttons = listOf(binding.tabUpcoming, binding.tabCompleted, binding.tabCancelled)
        for (btn in buttons) {
            if (btn == selectedButton) {
                btn.backgroundTintList = ColorStateList.valueOf(activeColor)
                btn.setTextColor(white)
                btn.strokeWidth = 0
            } else {
                btn.backgroundTintList = ColorStateList.valueOf(white)
                btn.setTextColor(inactiveColor)
                btn.strokeWidth = 2
            }
        }
    }

    private fun loadBookingsFromFirestore() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            mockSampleData()
            return
        }

        firestore.collection("bookings")
            .whereEqualTo("customerId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                allBookings = snapshot.toObjects(Booking::class.java)
                if (allBookings.isEmpty()) {
                    mockSampleData() // Dùng dữ liệu mẫu nếu chưa có dữ liệu thật trên Firebase
                } else {
                    filterBookings()
                }
            }
            .addOnFailureListener {
                mockSampleData()
            }
    }

    private fun filterBookings() {
        val filtered = when (currentTab) {
            "UPCOMING" -> allBookings.filter { it.status == "CONFIRMED" || it.status == "PENDING" }
            "COMPLETED" -> allBookings.filter { it.status == "COMPLETED" }
            "CANCELLED" -> allBookings.filter { it.status == "CANCELLED" }
            else -> allBookings
        }

        adapter.updateList(filtered)
        binding.layoutEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    // Dữ liệu mẫu giống hệt 2 thẻ trên ảnh mockup của bạn
    private fun mockSampleData() {
        allBookings = listOf(
            Booking(
                id = "1",
                bookingCode = "BK2026111501",
                facilityName = "SmashBadminton Center",
                courtName = "Sân 01",
                bookingDate = "15/11/2026",
                startTime = "18:00",
                endTime = "20:00",
                totalAmount = 160000.0,
                status = "CONFIRMED"
            ),
            Booking(
                id = "2",
                bookingCode = "RC2026111801",
                facilityName = "TopSpin Badminton Club",
                courtName = "Sân 02",
                bookingDate = "Thứ 3 và Thứ 5 hằng tuần",
                startTime = "19:00",
                endTime = "21:00",
                totalAmount = 320000.0,
                status = "PENDING",
                recurringBookingId = "rec_01"
            )
        )
        filterBookings()
    }
}