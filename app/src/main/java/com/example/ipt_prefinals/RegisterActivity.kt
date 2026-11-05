package com.example.ipt_prefinals

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ipt_prefinals.databinding.ActivityRegisterBinding
import com.example.ipt_prefinals.service.FirebaseManager

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            performRegistration()
        }

        binding.tvGoToLogin.setOnClickListener {
            finish()
        }
    }

    private fun performRegistration() {
        val fullName = binding.etRegName.text.toString().trim()
        val email = binding.etRegEmail.text.toString().trim()
        val password = binding.etRegPassword.text.toString().trim()
        val confirmPass = binding.etRegConfirmPassword.text.toString().trim()

        if (fullName.isEmpty()) {
            binding.etRegName.error = "Full Name is required"
            binding.etRegName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.etRegEmail.error = "Email address is required"
            binding.etRegEmail.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.etRegPassword.error = "Password must be at least 6 characters"
            binding.etRegPassword.requestFocus()
            return
        }

        if (password != confirmPass) {
            binding.etRegConfirmPassword.error = "Passwords do not match"
            binding.etRegConfirmPassword.requestFocus()
            return
        }

        binding.registerProgress.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        FirebaseManager.registerUser(email, password, fullName) { success, message ->
            binding.registerProgress.visibility = View.GONE
            binding.btnRegister.isEnabled = true

            if (success) {
                Toast.makeText(this, message ?: "Registration successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            } else {
                Toast.makeText(this, message ?: "Registration failed.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
