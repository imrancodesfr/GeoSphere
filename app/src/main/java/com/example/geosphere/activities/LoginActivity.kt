package com.example.geosphere.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.geosphere.R
import com.example.geosphere.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.geosphere.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firebaseHelper = FirebaseHelper()

        setupClickListeners()
        startAnimations()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.tvAdminLogin.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.tvForgotPassword.setOnClickListener {
            // Handle forgot password
            resetPassword()
        }
    }

    private fun startAnimations() {
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        binding.ivLogo.startAnimation(fadeIn)
        binding.tilEmail.startAnimation(slideUp)
        binding.tilPassword.startAnimation(slideUp)
        binding.btnLogin.startAnimation(slideUp)
        binding.tvRegister.startAnimation(slideUp)
    }

    private fun loginUser() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (validateInput(email, password)) {
            showProgress(true)

            lifecycleScope.launch {
                try {
                    val result = auth.signInWithEmailAndPassword(email, password).await()

                    // Check if user is admin
                    val userId = result.user?.uid ?: ""
                    val isAdmin = firebaseHelper.verifyAdmin(userId)

                    showProgress(false)

                    if (isAdmin) {
                        Toast.makeText(this@LoginActivity, "Admin login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, AdminDashboardActivity::class.java))
                    } else {
                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    }

                    finish()
                } catch (e: Exception) {
                    showProgress(false)
                    Toast.makeText(this@LoginActivity, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun resetPassword() {
        val email = binding.etEmail.text.toString().trim()

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.error = "Email required"
            return
        }

        showProgress(true)

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showProgress(false)
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.error = "Email required"
            isValid = false
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.error = "Password required"
            isValid = false
        }

        return isValid
    }

    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !show
        binding.tvRegister.isEnabled = !show
    }
}