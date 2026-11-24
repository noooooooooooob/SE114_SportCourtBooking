package com.se114.courtbooking.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.se114.courtbooking.ui.home.HomeFeaturedCourt
import com.se114.courtbooking.ui.home.HomeFeaturedCourtAdapter
import com.se114.courtbooking.MainActivity
import com.se114.courtbooking.R
import com.se114.courtbooking.databinding.FragmentHomeBinding
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var selectedDate: LocalDate = LocalDate.now(VIETNAM_ZONE)
    private var selectedDistrict: String = "Quận 1"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFeaturedCourts()
        updateSearchSelections()
        setupClickListeners()
    }

    private fun setupFeaturedCourts() {
        binding.featuredCourts.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.featuredCourts.adapter = HomeFeaturedCourtAdapter().apply {
            submitList(
                listOf(
                    HomeFeaturedCourt(
                        name = getString(R.string.home_demo_court_name),
                        address = getString(R.string.home_demo_court_address),
                        price = "80.000",
                        rating = getString(R.string.home_demo_rating),
                        imageRes = R.drawable.court_photo_placeholder
                    ),
                    HomeFeaturedCourt(
                        name = getString(R.string.home_demo_court_name_two),
                        address = getString(R.string.home_demo_court_address_two),
                        price = "90.000",
                        rating = "4.7",
                        imageRes = R.drawable.court_photo_placeholder
                    )
                )
            )
        }
    }

    private fun setupClickListeners() {
        binding.btnChooseDate.setOnClickListener { showDatePicker() }
        binding.btnChooseDistrict.setOnClickListener { showDistrictPicker() }
        binding.btnFindCourts.setOnClickListener { openCourtSearch() }
        binding.btnSeeMore.setOnClickListener { openCourtSearch() }
        binding.btnNotifications.setOnClickListener {
            Toast.makeText(requireContext(), R.string.home_placeholder_tab, Toast.LENGTH_SHORT).show()
        }
        binding.etHomeSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                openCourtSearch()
                true
            } else false
        }
    }

    private fun showDatePicker() {
        val selection = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.home_date_picker_title)
            .setSelection(selection)
            .build()
        picker.addOnPositiveButtonClickListener { timestamp ->
            selectedDate = Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).toLocalDate()
            updateSearchSelections()
        }
        picker.show(childFragmentManager, "home_date_picker")
    }

    private fun showDistrictPicker() {
        val districts = arrayOf("Quận 1", "Quận 3", "Quận 5", "Quận 7", "Bình Thạnh", "Thủ Đức")
        val selectedIndex = districts.indexOf(selectedDistrict).coerceAtLeast(0)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.home_district_picker_title)
            .setSingleChoiceItems(districts, selectedIndex) { dialog, index ->
                selectedDistrict = districts[index]
                updateSearchSelections()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.court_search_cancel, null)
            .show()
    }

    private fun updateSearchSelections() {
        val today = LocalDate.now(VIETNAM_ZONE)
        binding.tvHomeDate.text = if (selectedDate == today) {
            getString(R.string.home_today)
        } else {
            selectedDate.format(DATE_LABEL)
        }
        binding.tvHomeDistrict.text = selectedDistrict
    }

    private fun openCourtSearch() {
        // Tự động trượt sang Tab 1 (Tìm sân) trên MainActivity
        (activity as? MainActivity)?.findViewById<ViewPager2>(R.id.mainViewPager)?.setCurrentItem(1, true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        val VIETNAM_ZONE: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
        val DATE_LABEL: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM", Locale.forLanguageTag("vi-VN"))
    }
}