package com.example.lab3android

import android.content.Context
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.lab3android.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    companion object {      // Companion object содержит константы, общие для всего класса
        private const val PREFS_NAME = "AppSettings"        // Имя файла SharedPreferences
        private const val KEY_DARK_THEME = "dark_theme"     // Ключ для хранения темы
    }

    //private lateinit var themeRadioGroup: RadioGroup
    //private lateinit var dayRadio: RadioButton
    //private lateinit var nightRadio: RadioButton
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        // Применяем тему
        ThemeUtils.applySavedTheme(this)

        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_settings)

        //themeRadioGroup = findViewById(R.id.themeRadioGroup)
        //dayRadio = findViewById(R.id.dayThemeRadio)
        //nightRadio = findViewById(R.id.nightThemeRadio)

        // Получаем SharedPreferences для чтения сохраненных настроек
        val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Получаем значение темы (true - темная, false - светлая), по умолчанию светла
        val isDark = sharedPref.getBoolean(KEY_DARK_THEME, false)

        // Устанавливаем корректный выбор
        if (isDark) binding.nightThemeRadio.isChecked = true else binding.dayThemeRadio.isChecked = true

        binding.themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            // Обработчик вызывается при изменении выбора радиокнопок
            when (checkedId) {
                R.id.dayThemeRadio -> setThemePreference(false)
                R.id.nightThemeRadio -> setThemePreference(true)
            }
        }
    }

    // Метод для установки предпочтения темы
    private fun setThemePreference(isDark: Boolean) {
        // Получаем SharedPreferences для записи
        val sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Сохраняем выбор темы в SharedPreferences
        sharedPref.edit().putBoolean(KEY_DARK_THEME, isDark).apply()

        val theme = if (isDark) "dark" else "light"     // Преобразуем boolean в строковое представление темы

        //  сохраняем тему в БД текущего пользователя
        val userLogin = sharedPref.getString("current_user", null)
        if (userLogin != null) {
            Thread {
                val db = Database(this)
                db.setUserTheme(userLogin, theme)
            }.start()
        }

        // Применяем тему глобально для всего приложения
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        // Перерисовываем текущую Activity
        recreate()
    }
}
