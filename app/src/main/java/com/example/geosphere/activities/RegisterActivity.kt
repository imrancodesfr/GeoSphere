package com.example.geosphere.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.geosphere.R
import com.example.geosphere.databinding.ActivityRegisterBinding
import com.example.geosphere.models.User
import com.example.geosphere.utils.FirebaseHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegisterActivity : BaseActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firebaseHelper = FirebaseHelper(this)

        setupClickListeners()
        startAnimations()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvLogin.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun startAnimations() {
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        binding.ivLogo.startAnimation(fadeIn)
        binding.tilUsername.startAnimation(slideUp)
        binding.tilEmail.startAnimation(slideUp)
        binding.tilPassword.startAnimation(slideUp)
        binding.tilConfirmPassword.startAnimation(slideUp)
        binding.btnRegister.startAnimation(slideUp)
        binding.tvLogin.startAnimation(slideUp)
    }

    private fun registerUser() {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (validateInput(username, email, password, confirmPassword)) {
            showProgress(true)

            lifecycleScope.launch {
                try {
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    val firebaseUser = result.user ?: throw Exception("Registration failed")
                    val userId = firebaseUser.uid

                    // Save username to Firebase Auth Profile
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()
                    firebaseUser.updateProfile(profileUpdates).await()

                    // Create user in database
                    val user = User(
                        uid = userId,
                        email = email,
                        username = username,
                        createdAt = System.currentTimeMillis()
                    )

                    firebaseHelper.createUser(user)

                    showProgress(false)
                    Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                    finish()
                } catch (e: Exception) {
                    showProgress(false)
                    Toast.makeText(this@RegisterActivity, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateInput(username: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        if (TextUtils.isEmpty(username)) {
            binding.etUsername.error = "Username required"
            isValid = false
        }

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.error = "Email required"
            isValid = false
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.error = "Password required"
            isValid = false
        } else if (password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            isValid = false
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            binding.etConfirmPassword.error = "Please confirm password"
            isValid = false
        } else if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            isValid = false
        }

        return isValid
    }

    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !show
        binding.tvLogin.isEnabled = !show
    }
}