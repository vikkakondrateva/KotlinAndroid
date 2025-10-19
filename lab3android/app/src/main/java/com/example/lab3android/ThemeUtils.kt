package com.example.lab3android

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

object ThemeUtils {

    private const val PREFS_NAME = "AppSettings"
    private const val KEY_DARK_THEME = "dark_theme"

    fun applySavedTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean(KEY_DARK_THEME, false)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
