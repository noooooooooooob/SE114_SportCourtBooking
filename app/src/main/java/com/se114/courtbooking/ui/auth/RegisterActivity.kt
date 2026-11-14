package com.se114.courtbooking.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.se114.courtbooking.data.model.enums.Role
import com.se114.courtbooking.data.model.User
import com.se114.courtbooking.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Nút Back (<) và nút chuyển sang Đăng nhập
        binding.btnBack.setOnClickListener { finish() }
        binding.tvGoToLogin.setOnClickListener { finish() }

        // Bấm nút "Đăng ký"
        binding.btnRegister.setOnClickListener {
            handleRegister()
        }
    }

    private fun handleRegister() {
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        // 1. Kiểm tra tính hợp lệ
        if (fullName.isEmpty()) {
            binding.etFullName.error = "Vui lòng nhập họ và tên"
            binding.etFullName.requestFocus()
            return
        }
        if (email.isEmpty()) {
            binding.etEmail.error = "Vui lòng nhập email"
            binding.etEmail.requestFocus()
            return
        }
        if (password.length < 6) {
            binding.etPassword.error = "Mật khẩu tối thiểu 6 ký tự"
            binding.etPassword.requestFocus()
            return
        }
        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Mật khẩu xác nhận không khớp"
            binding.etConfirmPassword.requestFocus()
            return
        }

        // 2. Gọi Firebase Auth tạo tài khoản
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Đang xử lý..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid ?: ""

                // 3. Tạo đối tượng User và lưu vào Cloud Firestore
                val newUser = User(
                    id = uid,
                    fullName = fullName,
                    email = email,
                    role = Role.CUSTOMER
                )

                firestore.collection("users").document(uid).set(newUser)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Đăng ký thành công! Hãy đăng nhập.", Toast.LENGTH_SHORT).show()
                        finish() // Quay về màn Đăng nhập
                    }
                    .addOnFailureListener { e ->
                        binding.btnRegister.isEnabled = true
                        binding.btnRegister.text = "Đăng ký"
                        Toast.makeText(this, "Lỗi lưu dữ liệu: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "Đăng ký"
                Toast.makeText(this, "Đăng ký thất bại: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}