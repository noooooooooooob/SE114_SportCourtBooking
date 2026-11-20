package com.se114.courtbooking.ui.court

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.se114.courtbooking.R
import com.se114.courtbooking.data.model.Facility
import com.se114.courtbooking.data.repository.FacilityRepository
import com.se114.courtbooking.databinding.ActivityCourtSearchBinding
import com.se114.courtbooking.databinding.DialogCourtFiltersBinding
import com.se114.courtbooking.databinding.HeaderCourtSearchBinding
import java.text.Collator
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class CourtSearchActivity : AppCompatActivity() {
    private lateinit var header: HeaderCourtSearchBinding
    private val repository by lazy { FacilityRepository() }
    private val adapter = FacilityAdapter(::showFacility)

    private var facilities: List<Facility>? = null
    private var availableIds: Set<String>? = null
    private var resultsReady = false
    private var searchRequest = 0

    private var district: String? = null
    private var date: LocalDate? = null
    private var appliedDistrict: String? = null
    private var appliedDate: LocalDate? = null
    private var keyword = ""
    private var maxPrice: Long? = null
    private var minRating = 0.0
    private var sort = FacilitySort.NAME
    private var location: SearchLocation? = null

    @Suppress("DEPRECATION")
    private var locationRequest: androidx.core.os.CancellationSignal? = null

    private val locationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) locate()
        else notifyUser(R.string.court_search_location_denied)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        val binding = ActivityCourtSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val results = binding.courtResults
        results.layoutManager = LinearLayoutManager(this)
        header = HeaderCourtSearchBinding.inflate(layoutInflater, results, false)
        results.adapter = ConcatAdapter(HeaderAdapter(header.root), adapter)
        ViewCompat.setOnApplyWindowInsetsListener(results) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottomGap = (16 * resources.displayMetrics.density).toInt()
            view.updatePadding(left = bars.left, top = bars.top, right = bars.right,
                bottom = maxOf(bars.bottom, keyboard.bottom) + bottomGap)
            insets
        }

        savedInstanceState?.let(::restoreSearch)
        updateControls()
        header.btnBack.setOnClickListener { finish() }
        header.btnDate.setOnClickListener { showDateChoices() }
        header.btnDistrict.setOnClickListener { chooseDistrict() }
        header.btnFilters.setOnClickListener { showFilters() }
        header.btnSort.setOnClickListener { showSort() }
        header.btnSearch.setOnClickListener { applySearch() }
        header.etCourtName.setOnEditorActionListener { _, action, _ ->
            if (action != EditorInfo.IME_ACTION_SEARCH) return@setOnEditorActionListener false
            if (facilities != null) applySearch()
            true
        }
        header.btnRetry.setOnClickListener {
            if (facilities == null) loadFacilities() else checkDate()
        }

        loadFacilities()
    }

    private fun loadFacilities() {
        header.btnSearch.isEnabled = false
        header.btnDistrict.isEnabled = false
        showState(R.string.court_search_loading, loading = true)
        repository.getActiveFacilities()
            .addOnSuccessListener { catalog ->
                if (!isDestroyed && !isFinishing) {
                    facilities = catalog
                    header.btnSearch.isEnabled = true
                    header.btnDistrict.isEnabled = true
                    checkDate()
                }
            }
            .addOnFailureListener { error ->
                if (!isDestroyed && !isFinishing) {
                    Log.w(TAG, "Unable to read facilities", error)
                    showState(R.string.court_search_error, R.string.court_search_error_hint, retry = true)
                }
            }
    }

    private fun applySearch() {
        getSystemService(InputMethodManager::class.java).hideSoftInputFromWindow(header.etCourtName.windowToken, 0)
        header.etCourtName.clearFocus()
        keyword = header.etCourtName.text.toString().trim()
        appliedDistrict = district
        appliedDate = date
        checkDate()
    }

    private fun checkDate() {
        val request = ++searchRequest
        val selected = appliedDate
        availableIds = null
        if (selected == null) {
            resultsReady = true
            renderResults()
            return
        }

        resultsReady = false
        adapter.submitList(emptyList())
        showState(R.string.court_search_checking_date, loading = true)
        repository.getAvailableFacilityIds(selected.toString())
            .addOnSuccessListener { ids ->
                if (request == searchRequest && !isDestroyed && !isFinishing) {
                    availableIds = ids
                    resultsReady = true
                    renderResults()
                }
            }
            .addOnFailureListener { error ->
                if (request == searchRequest && !isDestroyed && !isFinishing) {
                    Log.w(TAG, "Unable to check court availability", error)
                    showState(R.string.court_search_schedule_error, R.string.court_search_error_hint, retry = true)
                }
            }
    }

    private fun renderResults() {
        if (!resultsReady) return
        val results = searchFacilities(
            facilities.orEmpty(), keyword, appliedDistrict, maxPrice,
            minRating, sort, location, availableIds
        )
        adapter.submitList(results)
        if (results.isEmpty()) {
            showState(R.string.court_search_empty, R.string.court_search_empty_hint)
        } else {
            header.statePanel.isVisible = false
        }
    }

    private fun showState(@StringRes title: Int, @StringRes hint: Int = 0, loading: Boolean = false, retry: Boolean = false) {
        header.stateTitle.setText(title)
        header.stateHint.isVisible = hint != 0
        if (hint != 0) header.stateHint.setText(hint)
        header.loadingSpinner.isVisible = loading
        header.btnRetry.isVisible = retry
        header.statePanel.isVisible = true
    }

    private fun updateControls() {
        header.btnDate.text = date?.format(dateLabel) ?: getString(R.string.court_search_date)
        header.btnDate.contentDescription = date?.let { "${getString(R.string.court_search_optional_date)} ${it.format(fullDateLabel)}" }
            ?: getString(R.string.court_search_date)
        header.btnDistrict.text = district ?: getString(R.string.court_search_district)
        header.btnSort.setText(sortLabels[sort.ordinal])
        val count = listOf(maxPrice != null, minRating > 0).count { it }
        header.btnFilters.text = if (count == 0) getString(R.string.court_search_filters)
        else getString(R.string.court_search_filter_count, count)
    }

    private fun showDateChoices() {
        val earliest = LocalDate.now(vietnamZone)
        val initial = date?.coerceAtLeast(earliest) ?: earliest
        val picker = DatePickerDialog(this, { _, year, month, day ->
            date = LocalDate.of(year, month + 1, day)
            updateControls()
        }, initial.year, initial.monthValue - 1, initial.dayOfMonth)
        picker.datePicker.minDate = earliest.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        picker.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.court_search_apply), picker)
        picker.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.court_search_cancel), picker)
        picker.setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.court_search_any_date)) { _, _ ->
            date = null
            updateControls()
        }
        picker.show()
    }

    private fun chooseDistrict() {
        val districts = facilities.orEmpty().map { it.district }.filter { it.isNotBlank() }.distinct()
            .sortedWith(Collator.getInstance(vietnameseLocale))
        val labels = listOf(getString(R.string.court_search_all_districts)) + districts
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.court_search_district)
            .setSingleChoiceItems(labels.toTypedArray(), districts.indexOf(district) + 1) { dialog, index ->
                district = districts.getOrNull(index - 1)
                updateControls()
                dialog.dismiss()
            }
            .setNegativeButton(R.string.court_search_cancel, null)
            .show()
    }

    private fun showFilters() {
        val filters = DialogCourtFiltersBinding.inflate(layoutInflater)
        filters.maxPrice.setText(maxPrice?.toString().orEmpty())
        val labels = ratingChoices.map { value ->
            if (value == 0.0) getString(R.string.court_search_any_rating)
            else getString(R.string.court_search_rating_option, oneDecimal(value))
        }
        filters.minRating.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        filters.minRating.setSelection(ratingChoices.indexOf(minRating).coerceAtLeast(0))
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.court_search_filters)
            .setView(filters.root)
            .setNegativeButton(R.string.court_search_cancel, null)
            .setNeutralButton(R.string.court_search_reset) { _, _ ->
                maxPrice = null
                minRating = 0.0
                updateControls()
                renderResults()
            }
            .setPositiveButton(R.string.court_search_apply) { _, _ ->
                maxPrice = filters.maxPrice.text.toString().toLongOrNull()
                minRating = ratingChoices[filters.minRating.selectedItemPosition]
                updateControls()
                renderResults()
            }
            .show()
    }

    private fun showSort() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.court_search_sort_title)
            .setSingleChoiceItems(sortLabels.map { getString(it) }.toTypedArray(), sort.ordinal) { dialog, index ->
                dialog.dismiss()
                locationRequest?.cancel()
                locationRequest = null
                val selection = FacilitySort.entries[index]
                if (selection == FacilitySort.DISTANCE && location == null) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                        locate()
                    } else {
                        locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    }
                } else {
                    sort = selection
                    updateControls()
                    renderResults()
                }
            }
            .show()
    }

    @SuppressLint("MissingPermission")
    @Suppress("DEPRECATION")
    private fun locate() {
        val manager = getSystemService(LocationManager::class.java)
        val providers = manager.getProviders(true)
        val provider = listOf("fused", LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
            .firstOrNull { it in providers }
        if (provider == null) {
            notifyUser(R.string.court_search_location_missing)
            return
        }

        val request = androidx.core.os.CancellationSignal()
        locationRequest = request
        notifyUser(R.string.court_search_location_loading)
        try {
            LocationManagerCompat.getCurrentLocation(manager, provider, request, ContextCompat.getMainExecutor(this)) { fix ->
                if (locationRequest !== request || isDestroyed || isFinishing) return@getCurrentLocation
                locationRequest = null
                if (fix == null) {
                    notifyUser(R.string.court_search_location_missing)
                } else {
                    location = SearchLocation(fix.latitude, fix.longitude)
                    sort = FacilitySort.DISTANCE
                    updateControls()
                    renderResults()
                }
            }
        } catch (error: SecurityException) {
            locationRequest = null
            notifyUser(R.string.court_search_location_denied)
        } catch (error: IllegalArgumentException) {
            locationRequest = null
            notifyUser(R.string.court_search_location_missing)
        }
    }

    private fun showFacility(facility: Facility) {
        val details = buildList {
            if (facility.address.isNotBlank()) add(facility.address)
            add(priceText(facility.minPrice))
            add(ratingText(facility))
            if (facility.openTime.isNotBlank() && facility.closeTime.isNotBlank()) {
                add(getString(R.string.court_search_open_hours, facility.openTime, facility.closeTime))
            }
            if (facility.amenities.isNotEmpty()) add(facility.amenities.joinToString(" • "))
            if (facility.description.isNotBlank()) add(facility.description)
        }
        MaterialAlertDialogBuilder(this)
            .setTitle(facility.name)
            .setMessage(details.joinToString("\n\n"))
            .setPositiveButton(R.string.court_search_close, null)
            .show()
    }

    private fun notifyUser(@StringRes message: Int) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("draftKeyword", header.etCourtName.text.toString())
        outState.putString("district", district)
        outState.putString("date", date?.toString())
        outState.putString("appliedDistrict", appliedDistrict)
        outState.putString("appliedDate", appliedDate?.toString())
        outState.putString("keyword", keyword)
        maxPrice?.let { outState.putLong("maxPrice", it) }
        outState.putDouble("minRating", minRating)
        outState.putString("sort", sort.name)
        location?.let { outState.putDoubleArray("location", doubleArrayOf(it.latitude, it.longitude)) }
    }

    private fun restoreSearch(state: Bundle) {
        header.etCourtName.setText(state.getString("draftKeyword").orEmpty())
        district = state.getString("district")
        date = state.getString("date")?.let(LocalDate::parse)
        appliedDistrict = state.getString("appliedDistrict")
        appliedDate = state.getString("appliedDate")?.let(LocalDate::parse)
        keyword = state.getString("keyword").orEmpty()
        maxPrice = state.getLong("maxPrice").takeIf { state.containsKey("maxPrice") }
        minRating = state.getDouble("minRating")
        sort = state.getString("sort")?.let(FacilitySort::valueOf) ?: FacilitySort.NAME
        location = state.getDoubleArray("location")?.let { SearchLocation(it[0], it[1]) }
    }

    override fun onDestroy() {
        locationRequest?.cancel()
        super.onDestroy()
    }

    private class HeaderAdapter(private val header: View) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount(): Int = 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
            object : RecyclerView.ViewHolder(header) {}

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = Unit
    }

    private companion object {
        const val TAG = "CourtSearch"
        val vietnamZone: ZoneId = ZoneId.of("Asia/Ho_Chi_Minh")
        val dateLabel: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM", vietnameseLocale)
        val fullDateLabel: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", vietnameseLocale)
        val sortLabels = intArrayOf(
            R.string.court_search_sort_name, R.string.court_search_sort_distance,
            R.string.court_search_sort_price, R.string.court_search_sort_rating
        )
        val ratingChoices = listOf(0.0, 3.0, 4.0, 4.5)
    }
}
