package com.se114.courtbooking.ui.auth

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.se114.courtbooking.R

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Bấm nút mũi tên Back (<) -> Quay lại màn hình trước
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        // Bấm vào chữ "Đăng nhập" -> Quay lại LoginActivity
        val tvGoToLogin = findViewById<TextView>(R.id.tvGoToLogin)
        tvGoToLogin.setOnClickListener {
            finish()
        }
    }
}