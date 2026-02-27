package com.example.geosphere.activities

import android.os.Bundle
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import com.example.geosphere.utils.ThemeHelper

/**
 * All activities must extend BaseActivity so the saved theme
 * is applied before any views are inflated.
 */
abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // ← MUST be before super.onCreate() so the theme is set before
        //   the window decor and any content views are inflated.
        ThemeHelper.applyTheme(this)
        super.onCreate(savedInstanceState)
    }

    /**
     * Resolves the theme's colorPrimary attribute at runtime.
     * Use this programmatically wherever you need the active theme color.
     */
    protected fun resolveThemeColor(attr: Int): Int {
        val tv = TypedValue()
        theme.resolveAttribute(attr, tv, true)
        return tv.data
    }
}
