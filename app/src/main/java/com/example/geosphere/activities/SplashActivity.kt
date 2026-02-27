package com.example.geosphere.activities

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import com.example.geosphere.R
import com.example.geosphere.databinding.ActivitySplashBinding
import com.example.geosphere.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)

            auth = FirebaseAuth.getInstance()

            // Start animations
            startAnimations()

            // Navigate after delay - FIXED VERSION
            lifecycleScope.launch {
                delay(Constants.SPLASH_DURATION)
                navigateToNextScreen()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If anything fails, go directly to login
            navigateToNextScreen()
        }
    }

    private fun startAnimations() {
        try {
            // Fade in animation for logo
            val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            binding.ivLogo.startAnimation(fadeIn)

            // Slide up animation for text
            val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
            binding.tvAppName.startAnimation(slideUp)
            binding.tvTagline.startAnimation(slideUp)

            // Rotate animation for globe
            val rotate = AnimationUtils.loadAnimation(this, R.anim.rotate)
            binding.ivGlobe.startAnimation(rotate)
        } catch (e: Exception) {
            e.printStackTrace()
            // Continue without animations if they fail
        }
    }

    private fun navigateToNextScreen() {
        try {
            val currentUser = auth.currentUser
            val intent = if (currentUser != null) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, LoginActivity::class.java)
            }

            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }
}