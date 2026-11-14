package com.se114.courtbooking

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.se114.courtbooking.data.model.enums.Role
import com.se114.courtbooking.ui.admin.AdminActivity
import com.se114.courtbooking.ui.auth.LoginActivity
import com.se114.courtbooking.utils.SessionManager

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)

        // 1. Kiểm tra trạng thái đăng nhập
        if (!sessionManager.isLoggedIn()) {
            // Chưa đăng nhập -> Chuyển sang màn hình Đăng nhập
            navigateTo(LoginActivity::class.java)
        } else {
            // 2. Đã đăng nhập -> Kiểm tra Role để phân luồng đúng màn hình
            when (sessionManager.getUserRole()) {
                Role.CUSTOMER -> {
                    // Khách hàng -> Vào màn hình đặt sân
                    navigateTo(MainActivity::class.java)
                }
                Role.STAFF, Role.ADMIN -> {
                    // Chủ sân / Nhân viên -> Vào màn hình quản trị
                    navigateTo(AdminActivity::class.java)
                }
            }
        }
    }

    // Hàm chuyển màn hình và đóng luôn SplashActivity
    private fun navigateTo(destination: Class<*>) {
        val intent = Intent(this, destination)
        startActivity(intent)
        finish() // Đóng Splash để người dùng bấm nút Back không bị quay lại đây
    }
}