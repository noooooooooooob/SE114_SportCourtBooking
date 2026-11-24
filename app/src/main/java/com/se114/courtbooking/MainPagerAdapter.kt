package com.se114.courtbooking

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.se114.courtbooking.ui.booking.MyBookingsFragment
import com.se114.courtbooking.ui.court.CourtSearchFragment
import com.se114.courtbooking.ui.home.HomeFragment
import com.se114.courtbooking.ui.profile.ProfileFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> CourtSearchFragment()
            2 -> MyBookingsFragment()
            3 -> ProfileFragment()
            else -> HomeFragment()
        }
    }
}