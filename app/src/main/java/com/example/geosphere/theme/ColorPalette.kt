package com.example.geosphere.theme

object ColorPalette {
    // Contemporary Focus Palette
    const val DarkBlue1 = "#2C3E50"
    const val DarkBlue2 = "#34495E"
    const val Gray1 = "#7F8C8D"
    const val LightGray1 = "#BDC3C7"
    const val LightGray2 = "#ECF0F1"

    // Strategic Vision Palette
    const val BoldBlue1 = "#2980B9"
    const val BoldBlue2 = "#3498DB"
    const val BoldBlue3 = "#5DADE2"
    const val SubtleGreen = "#85C1AE"
    const val SubtleYellow = "#F7DC6F"

    // Innovative Spirit Palette
    const val Turquoise = "#1ABC9C"
    const val DeepTeal = "#16A085"
    const val BrightYellow = "#F1C40F"
    const val BoldOrange = "#E67E22"
    const val StrikingRed = "#E74C3C"

    // Bold Leadership Palette
    const val StrongRed1 = "#C0392B"
    const val StrongRed2 = "#E74C3C"
    const val BoldYellow1 = "#F39C12"
    const val BoldYellow2 = "#F1C40F"
    const val CalmGreen = "#2ECC71"

    // Refined Professionalism
    const val AccentPurple = "#7D3C98"

    fun getPrimaryColor(isDarkMode: Boolean): String {
        return if (isDarkMode) DarkBlue2 else BoldBlue2
    }

    fun getAccentColor(isDarkMode: Boolean): String {
        return if (isDarkMode) Turquoise else BoldBlue1
    }
}