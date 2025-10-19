package com.example.lab3android

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.content.Intent

class SettingsActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "AppSettings"
        private const val KEY_DARK_THEME = "dark_theme"
    }

    private lateinit var themeRadioGroup: RadioGroup
    private lateinit var dayRadio: RadioButton
    private lateinit var nightRadio: RadioButton

    override fun onCreate(savedInstanceState: Bundle?) {
        // ✅ Применяем тему ДО super.onCreate()
        ThemeUtils.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        themeRadioGroup = findViewById(R.id.themeRadioGroup)
        dayRadio = findViewById(R.id.dayThemeRadio)
        nightRadio = findViewById(R.id.nightThemeRadio)

        val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDark = sharedPref.getBoolean(KEY_DARK_THEME, false)

        // Устанавливаем корректный выбор
        if (isDark) nightRadio.isChecked = true else dayRadio.isChecked = true

        themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.dayThemeRadio -> setThemePreference(false)
                R.id.nightThemeRadio -> setThemePreference(true)
            }
        }
    }

    private fun setThemePreference(isDark: Boolean) {
        val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPref.edit().putBoolean(KEY_DARK_THEME, isDark).apply()

        val theme = if (isDark) "dark" else "light"

        // ✅ сохраняем тему в БД текущего пользователя
        val userLogin = sharedPref.getString("current_user", null)
        if (userLogin != null) {
            val db = Database(this)
            db.setUserTheme(userLogin, theme)
        }

        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        // ✅ просто перерисовываем текущую Activity
        // чтобы изменения применились сразу
        recreate()
    }
}
