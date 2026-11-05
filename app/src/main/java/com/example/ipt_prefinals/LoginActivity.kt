package com.example.ipt_prefinals

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ipt_prefinals.databinding.ActivityLoginBinding
import com.example.ipt_prefinals.service.FirebaseManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is already logged in
        if (FirebaseManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun performLogin() {
        val email = binding.etLoginEmail.text.toString().trim()
        val password = binding.etLoginPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.etLoginEmail.error = "Email address required"
            binding.etLoginEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.etLoginPassword.error = "Password required"
            binding.etLoginPassword.requestFocus()
            return
        }

        binding.loginProgress.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        FirebaseManager.loginUser(email, password) { success, message ->
            binding.loginProgress.visibility = View.GONE
            binding.btnLogin.isEnabled = true

            if (success) {
                Toast.makeText(this, message ?: "Sign in successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, message ?: "Authentication failed.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
