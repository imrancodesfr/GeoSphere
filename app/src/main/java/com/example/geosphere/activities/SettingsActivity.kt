package com.example.geosphere.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(this, "Settings - Coming Soon!", Toast.LENGTH_SHORT).show()
        finish()
    }
}