package com.example.geosphere.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.geosphere.R

object ThemeHelper {

    const val THEME_MIDNIGHT    = "midnight"    // Default dark blue + teal
    const val THEME_OCEAN       = "ocean"       // Deep navy + cyan
    const val THEME_SUNSET      = "sunset"      // Dark charcoal + orange
    const val THEME_FOREST      = "forest"      // Dark green + lime
    const val THEME_ROYAL       = "royal"       // Dark purple + gold

    private const val KEY_THEME = "app_theme"

    data class ThemeOption(
        val id: String,
        val label: String,
        val primaryColor: String,   // hex for preview circle
        val accentColor: String
    )

    val ALL_THEMES = listOf(
        ThemeOption(THEME_MIDNIGHT, "Midnight",  "#2C3E50", "#1ABC9C"),
        ThemeOption(THEME_OCEAN,   "Ocean",      "#1A3A5C", "#00BCD4"),
        ThemeOption(THEME_SUNSET,  "Sunset",     "#2D2D2D", "#E67E22"),
        ThemeOption(THEME_FOREST,  "Forest",     "#1B3A1F", "#4CAF50"),
        ThemeOption(THEME_ROYAL,   "Royal",      "#2D1B4E", "#FFD700")
    )

    fun getThemeResId(themeId: String): Int = when (themeId) {
        THEME_OCEAN   -> R.style.Theme_GeoSphere_Ocean
        THEME_SUNSET  -> R.style.Theme_GeoSphere_Sunset
        THEME_FOREST  -> R.style.Theme_GeoSphere_Forest
        THEME_ROYAL   -> R.style.Theme_GeoSphere_Royal
        else          -> R.style.Theme_GeoSphere   // THEME_MIDNIGHT default
    }

    fun saveTheme(context: Context, themeId: String) {
        prefs(context).edit().putString(KEY_THEME, themeId).apply()
    }

    fun getSavedTheme(context: Context): String =
        prefs(context).getString(KEY_THEME, THEME_MIDNIGHT) ?: THEME_MIDNIGHT

    fun applyTheme(context: Context) {
        // Call this before setContentView in each activity
        val themeId = getSavedTheme(context)
        (context as? android.app.Activity)?.setTheme(getThemeResId(themeId))
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
}
