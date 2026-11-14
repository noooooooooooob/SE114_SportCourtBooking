package com.se114.courtbooking.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.se114.courtbooking.MainActivity
import com.se114.courtbooking.data.model.enums.Role
import com.se114.courtbooking.databinding.ActivityLoginBinding
import com.se114.courtbooking.ui.admin.AdminActivity
import com.se114.courtbooking.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val sessionManager: SessionManager by lazy { SessionManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bấm chuyển sang Đăng ký
        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // Bấm nút "Đăng nhập"
        binding.btnLogin.setOnClickListener {
            handleLogin()
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.etEmail.error = "Vui lòng nhập email"
            binding.etEmail.requestFocus()
            return
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Vui lòng nhập mật khẩu"
            binding.etPassword.requestFocus()
            return
        }

        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Đang đăng nhập..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""

                // Đọc thông tin vai trò (Role) từ Firestore
                firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { document ->
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = "Đăng nhập"

                        val roleStr = document.getString("role")
                        val role = Role.fromString(roleStr)

                        // Lưu phiên đăng nhập
                        sessionManager.saveUserSession(uid, role)

                        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()

                        // Phân luồng màn hình theo vai trò
                        val targetActivity = when (role) {
                            Role.ADMIN, Role.STAFF -> AdminActivity::class.java
                            Role.CUSTOMER -> MainActivity::class.java
                        }

                        val intent = Intent(this, targetActivity)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                    .addOnFailureListener { e ->
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = "Đăng nhập"
                        Toast.makeText(this, "Lỗi lấy dữ liệu tài khoản: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Đăng nhập"
                Toast.makeText(this, "Đăng nhập thất bại: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}