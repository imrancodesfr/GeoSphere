package com.example.geosphere

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class GeoSphereApplication : Application() {

    // Use lazy initialization to ensure Firebase is ready when accessed
    val database: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    override fun onCreate() {
        super.onCreate()
        // Ensure Firebase is initialized (auto-init should already have happened)
        FirebaseApp.initializeApp(this)

        // Optional: enable offline persistence – must be called after initialization
        try {
            database.setPersistenceEnabled(true)
        } catch (e: Exception) {
            // Persistence can only be enabled once; ignore if already enabled
        }
    }
}