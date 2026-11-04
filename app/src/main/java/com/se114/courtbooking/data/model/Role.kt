package com.se114.courtbooking.data.model

enum class Role(val displayName: String) {
    CUSTOMER("Khách hàng"),
    STAFF("Nhân viên"),
    ADMIN("Chủ sân");

    companion object {
        // Hàm phụ: Chuyển từ String (lưu trên Database) sang Role an toàn
        fun fromString(roleStr: String?): Role {
            return entries.find { it.name.equals(roleStr, ignoreCase = true) } ?: CUSTOMER
        }
    }
}