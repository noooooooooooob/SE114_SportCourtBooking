package com.se114.courtbooking.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.se114.courtbooking.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Bấm vào chữ "Đăng ký ngay" -> Chuyển sang RegisterActivity
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)
        tvGoToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}