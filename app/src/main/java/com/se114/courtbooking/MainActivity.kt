package com.se114.courtbooking

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewpager2.widget.ViewPager2
import com.se114.courtbooking.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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

        setupViewPager()
        setupBottomNavClick()
    }

    private fun setupViewPager() {
        binding.mainViewPager.adapter = MainPagerAdapter(this)

        // LẮNG NGHE KHI NGƯỜI DÙNG QUẸT / VUỐT MÀN HÌNH
        binding.mainViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Cập nhật vạch xanh và màu sắc icon theo trang vừa vuốt tới
                updateBottomNavUI(position)
            }
        })
    }

    private fun setupBottomNavClick() {
        binding.navHome.setOnClickListener { binding.mainViewPager.setCurrentItem(0, true) }
        binding.navSearch.setOnClickListener { binding.mainViewPager.setCurrentItem(1, true) }
        binding.navBookings.setOnClickListener { binding.mainViewPager.setCurrentItem(2, true) }
        binding.navProfile.setOnClickListener { binding.mainViewPager.setCurrentItem(3, true) }
    }

    // Hàm đổi màu icon và di chuyển vạch xanh theo vị trí Tab (0, 1, 2, 3)
    private fun updateBottomNavUI(position: Int) {
        val activeColor = ContextCompat.getColor(this, R.color.smash_primary)
        val inactiveColor = ContextCompat.getColor(this, R.color.home_muted)
        val inkColor = ContextCompat.getColor(this, R.color.home_ink)

        // 1. Reset tất cả về trạng thái ẩn
        binding.indicatorHome.visibility = if (position == 0) View.VISIBLE else View.INVISIBLE
        binding.iconHome.setColorFilter(if (position == 0) activeColor else inkColor)
        binding.labelHome.setTextColor(if (position == 0) activeColor else inactiveColor)

        binding.indicatorSearch.visibility = if (position == 1) View.VISIBLE else View.INVISIBLE
        binding.iconSearch.setColorFilter(if (position == 1) activeColor else inkColor)
        binding.labelSearch.setTextColor(if (position == 1) activeColor else inactiveColor)

        binding.indicatorBookings.visibility = if (position == 2) View.VISIBLE else View.INVISIBLE
        binding.iconBookings.setColorFilter(if (position == 2) activeColor else inkColor)
        binding.labelBookings.setTextColor(if (position == 2) activeColor else inactiveColor)

        binding.indicatorProfile.visibility = if (position == 3) View.VISIBLE else View.INVISIBLE
        binding.iconProfile.setColorFilter(if (position == 3) activeColor else inkColor)
        binding.labelProfile.setTextColor(if (position == 3) activeColor else inactiveColor)
    }
}