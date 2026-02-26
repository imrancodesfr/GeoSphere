package com.example.geosphere.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val score = intent.getIntExtra("score", 0)
        val total = intent.getIntExtra("total", 0)
        Toast.makeText(this, "Score: $score/$total", Toast.LENGTH_LONG).show()
        finish()
    }
}