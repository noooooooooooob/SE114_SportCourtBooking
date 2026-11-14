package com.se114.courtbooking.utils

import androidx.core.content.edit

import android.content.Context
import android.content.SharedPreferences
import com.se114.courtbooking.data.model.enums.Role

class SessionManager(context: Context) {

    // 1. Khởi tạo SharedPreferences với tên file là "CourtBookingSession"
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "CourtBookingSession",
        Context.MODE_PRIVATE
    )

    // Khai báo các Key để lưu dữ liệu
    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_ROLE = "user_role"
    }

    // 2. Hàm lưu thông tin sau khi đăng nhập thành công
    fun saveUserSession(userId: String, role: Role) {
        prefs.edit {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USER_ID, userId)
            putString(KEY_USER_ROLE, role.name)
        }
    }

    // 3. Hàm kiểm tra đã đăng nhập chưa
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // 4. Hàm lấy Role của người dùng hiện tại
    fun getUserRole(): Role {
        val roleStr = prefs.getString(KEY_USER_ROLE, Role.CUSTOMER.name)
        return Role.fromString(roleStr)
    }

    // 5. Hàm lấy ID người dùng
    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    // 6. Hàm đăng xuất (xóa sạch dữ liệu trong phiên)
    fun clearSession() {
        prefs.edit { clear() }
    }
}