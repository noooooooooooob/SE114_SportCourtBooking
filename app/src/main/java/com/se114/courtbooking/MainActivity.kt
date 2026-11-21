package com.se114.courtbooking

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.se114.courtbooking.databinding.ActivityMainBinding
import com.se114.courtbooking.ui.court.CourtSearchActivity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var selectedDate: LocalDate = LocalDate.now(VIETNAM_ZONE)
    private var selectedDistrict: String = "Quận 1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.mainRoot) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bars.bottom)
            insets
        }

        setupFeaturedCourts()
        setupResponsiveLayout()
        updateSearchSelections()
        setupClickListeners()
    }

    /** Stacks the date, district, and search controls when the available row is too narrow. */
    private fun setupResponsiveLayout() {
        binding.homeFilterRow.addOnLayoutChangeListener { _, left, _, right, _, _, _, _, _ ->
            updateFilterLayout(right - left)
        }
        binding.homeFilterRow.post { updateFilterLayout(binding.homeFilterRow.width) }
    }

    private fun updateFilterLayout(availableWidthPx: Int) {
        if (availableWidthPx <= 0) return

        // 344dp leaves enough room for the date and district labels beside the fixed search button.
        val stackControls = availableWidthPx <
            resources.getDimensionPixelSize(R.dimen.home_dp_344)
        val orientation = if (stackControls) LinearLayout.VERTICAL else LinearLayout.HORIZONTAL
        if (binding.homeFilterRow.orientation == orientation) return

        binding.homeFilterRow.orientation = orientation
        (binding.homeFilterSelection.layoutParams as LinearLayout.LayoutParams).apply {
            width = if (stackControls) LinearLayout.LayoutParams.MATCH_PARENT else 0
            height = resources.getDimensionPixelSize(R.dimen.home_dp_56)
            weight = if (stackControls) 0f else 1f
            setMargins(0, 0, 0, 0)
        }
        (binding.btnFindCourts.layoutParams as LinearLayout.LayoutParams).apply {
            width = if (stackControls) {
                LinearLayout.LayoutParams.MATCH_PARENT
            } else {
                resources.getDimensionPixelSize(R.dimen.home_dp_104)
            }
            height = resources.getDimensionPixelSize(R.dimen.home_dp_56)
            weight = 0f
            val margin = resources.getDimensionPixelSize(R.dimen.home_dp_10)
            setMargins(if (stackControls) 0 else margin, if (stackControls) margin else 0, 0, 0)
        }
        binding.homeFilterRow.layoutParams = binding.homeFilterRow.layoutParams.apply {
            height = LinearLayout.LayoutParams.WRAP_CONTENT
        }
    }

    private fun setupFeaturedCourts() {
        binding.featuredCourts.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
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
        binding.btnNotifications.setOnClickListener { showPlaceholderMessage() }

        binding.etHomeSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                openCourtSearch()
                true
            } else {
                false
            }
        }

        binding.navHome.setOnClickListener { binding.mainRoot.smoothScrollToTop() }
        binding.navSearch.setOnClickListener { openCourtSearch() }
        binding.navBookings.setOnClickListener { showPlaceholderMessage() }
        binding.navProfile.setOnClickListener { showPlaceholderMessage() }
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
        picker.show(supportFragmentManager, "home_date_picker")
    }

    private fun showDistrictPicker() {
        val districts = arrayOf("Quận 1", "Quận 3", "Quận 5", "Quận 7", "Bình Thạnh", "Thủ Đức")
        val selectedIndex = districts.indexOf(selectedDistrict).coerceAtLeast(0)
        MaterialAlertDialogBuilder(this)
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
        startActivity(Intent(this, CourtSearchActivity::class.java))
    }

    private fun showPlaceholderMessage() {
        Toast.makeText(this, R.string.home_placeholder_tab, Toast.LENGTH_SHORT).show()
    }

    private fun android.view.View.smoothScrollToTop() {
        (binding.root.getChildAt(0) as? android.widget.ScrollView)?.smoothScrollTo(0, 0)
    }

    private companion object {
        val VIETNAM_ZONE: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
        val DATE_LABEL: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM", Locale.forLanguageTag("vi-VN"))
    }
}
