package com.se114.courtbooking.ui.booking

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.se114.courtbooking.R
import com.se114.courtbooking.data.model.Booking
import com.se114.courtbooking.databinding.ActivityMyBookingsBinding

class MyBookingsFragment : Fragment() {

    private var _binding: ActivityMyBookingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: BookingAdapter
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var allBookings = listOf<Booking>()
    private var currentTab = "UPCOMING"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = ActivityMyBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabs()
        loadBookingsFromFirestore()
    }

    private fun setupRecyclerView() {
        adapter = BookingAdapter(emptyList()) { booking ->
            Toast.makeText(requireContext(), "Xem chi tiết mã: ${booking.bookingCode}", Toast.LENGTH_SHORT).show()
        }
        binding.rvBookings.layoutManager = LinearLayoutManager(requireContext())
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
        val context = requireContext()
        val activeColor = ContextCompat.getColor(context, R.color.smash_primary)
        val inactiveColor = ContextCompat.getColor(context, R.color.smash_text_sub)
        val white = ContextCompat.getColor(context, R.color.white)

        val buttons = listOf(binding.tabUpcoming, binding.tabCompleted, binding.tabCancelled)
        for (btn in buttons) {
            if (btn == selectedButton) {
                btn.backgroundTintList = ColorStateList.valueOf(activeColor)
                btn.setTextColor(white)
                btn.iconTint = ColorStateList.valueOf(white)
                btn.strokeWidth = 0
            } else {
                btn.backgroundTintList = ColorStateList.valueOf(white)
                btn.setTextColor(inactiveColor)
                btn.iconTint = ColorStateList.valueOf(inactiveColor)
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
                if (allBookings.isEmpty()) mockSampleData() else filterBookings()
            }
            .addOnFailureListener { mockSampleData() }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}