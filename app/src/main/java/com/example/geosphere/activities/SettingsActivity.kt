package com.example.geosphere.activities

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.geosphere.R
import com.example.geosphere.databinding.ActivitySettingsBinding
import com.example.geosphere.utils.Constants
import com.example.geosphere.utils.ThemeHelper

class SettingsActivity : BaseActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SharedPreferences
    private var currentTheme = ThemeHelper.THEME_MIDNIGHT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE)
        currentTheme = ThemeHelper.getSavedTheme(this)

        setupToolbar()
        loadSettings()
        setupAppInfo()
        setupListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.settings)
        }
    }

    // ───────────────────────────────────────────────────
    // Load saved preferences into UI controls
    // ───────────────────────────────────────────────────
    private fun loadSettings() {
        binding.switchSoundEffects.isChecked = prefs.getBoolean(Constants.KEY_SOUND_EFFECTS, true)
        binding.switchAnimations.isChecked   = prefs.getBoolean(Constants.KEY_ANIMATIONS, true)
        binding.switchNotifications.isChecked = prefs.getBoolean(Constants.KEY_NOTIFICATIONS, true)

        updateThemeLabel(currentTheme)
    }

    private fun updateThemeLabel(themeId: String) {
        val option = ThemeHelper.ALL_THEMES.find { it.id == themeId }
        binding.tvCurrentTheme.text = option?.label ?: "Midnight"
    }

    // ───────────────────────────────────────────────────
    // Populate the About section with app info
    // ───────────────────────────────────────────────────
    private fun setupAppInfo() {
        try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            binding.tvAppVersion.text = "Version $versionName"
        } catch (e: Exception) {
            binding.tvAppVersion.text = "Version 1.0.0"
        }
    }

    // ───────────────────────────────────────────────────
    // Wire up all interactive elements
    // ───────────────────────────────────────────────────
    private fun setupListeners() {
        // Sound
        binding.switchSoundEffects.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(Constants.KEY_SOUND_EFFECTS, checked).apply()
        }
        // Animations
        binding.switchAnimations.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(Constants.KEY_ANIMATIONS, checked).apply()
        }
        // Notifications
        binding.switchNotifications.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean(Constants.KEY_NOTIFICATIONS, checked).apply()
        }

        // Theme picker row toggle
        binding.rowTheme.setOnClickListener { toggleThemePicker() }

        // Feedback — email
        binding.rowFeedback.setOnClickListener { sendFeedbackEmail() }

        // Rate app — Play Store (falls back to browser)
        binding.rowRateApp.setOnClickListener { rateApp() }

        // Share app
        binding.rowShareApp.setOnClickListener { shareApp() }

        // Tappable links
        binding.tvContact.setOnClickListener {
            openEmail("imrancodesfr@gmail.com", "GeoSphere — Contact")
        }
        binding.tvGitHub.setOnClickListener {
            openUrl("https://github.com/imrancodesfr")
        }
    }

    // ───────────────────────────────────────────────────
    // Theme picker inline swatch panel
    // ───────────────────────────────────────────────────
    private var themePickerVisible = false

    private fun toggleThemePicker() {
        if (themePickerVisible) {
            binding.layoutThemePicker.visibility = View.GONE
            themePickerVisible = false
            return
        }

        // Build swatches if not yet built
        if (binding.layoutThemePicker.childCount == 0) {
            buildThemeSwatches()
        }
        binding.layoutThemePicker.visibility = View.VISIBLE
        themePickerVisible = true
    }

    private fun buildThemeSwatches() {
        val size  = (48 * resources.displayMetrics.density).toInt()
        val margin = (10 * resources.displayMetrics.density).toInt()

        ThemeHelper.ALL_THEMES.forEach { option ->
            val container = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity     = android.view.Gravity.CENTER
            }

            // Outer ring (shown when selected)
            val ring = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.parseColor(option.primaryColor))
                setStroke(
                    (3 * resources.displayMetrics.density).toInt(),
                    if (option.id == currentTheme) Color.WHITE else Color.TRANSPARENT
                )
            }

            val swatch = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).also { lp ->
                    lp.setMargins(margin, 0, margin, 4)
                }
                background = ring
                setOnClickListener { applySelectedTheme(option.id, ring) }
            }

            val label = TextView(this).apply {
                text      = option.label
                textSize  = 10f
                setTextColor(
                    if (option.id == currentTheme) Color.WHITE else Color.parseColor("#BDC3C7")
                )
                gravity   = android.view.Gravity.CENTER
            }

            container.addView(swatch)
            container.addView(label)
            binding.layoutThemePicker.addView(container)
        }
    }

    private fun applySelectedTheme(themeId: String, @Suppress("UNUSED_PARAMETER") ring: GradientDrawable) {
        if (themeId == currentTheme) return

        ThemeHelper.saveTheme(this, themeId)
        Toast.makeText(this, "Theme changed! Restarting…", Toast.LENGTH_SHORT).show()

        // Restart the whole app from the launcher activity to apply theme everywhere
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
        finishAffinity()
    }

    // ───────────────────────────────────────────────────
    // Feedback helpers
    // ───────────────────────────────────────────────────
    private fun sendFeedbackEmail() {
        openEmail(
            to       = "imrancodesfr@gmail.com",
            subject  = "GeoSphere Feedback",
            body     = "\n\n──────────\nApp: GeoSphere\nVersion: ${getVersionName()}\nDevice: ${android.os.Build.MODEL}"
        )
    }

    private fun rateApp() {
        // Try Play Store deep link, fall back to browser
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun shareApp() {
        val text = "🌍 Check out GeoSphere — test your geography knowledge!\n" +
                "https://play.google.com/store/apps/details?id=$packageName"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, "Share GeoSphere via"))
    }

    private fun openEmail(to: String, subject: String, body: String = "") {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            if (body.isNotEmpty()) putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUrl(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getVersionName(): String = try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
    } catch (e: Exception) { "1.0.0" }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}