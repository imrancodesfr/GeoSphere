package com.example.geosphere.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.geosphere.databinding.ActivityAdminLoginBinding

class AdminLoginActivity : BaseActivity() {

    private lateinit var binding: ActivityAdminLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnAdminLogin.setOnClickListener {
            val email = binding.etAdminEmail.text.toString().trim()
            val password = binding.etAdminPassword.text.toString().trim()

            // Hardcoded admin creds to avoid needing a real Firebase user
            if (email == "admin@geosphere.com" && password == "admin123") {
                Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AdminDashboardActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid admin credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }
}