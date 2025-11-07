package com.example.lab3android

import android.content.Context                          // Класс для доступа к ресурсам приложения
import androidx.appcompat.app.AppCompatDelegate         // Класс для управления темами

object ThemeUtils {     // Object (синглтон) для утилит работы с темами

    // Константы для работы с SharedPreferences
    private const val PREFS_NAME = "AppSettings"        // Имя файла настроек
    private const val KEY_DARK_THEME = "dark_theme"     // Ключ для хранения темы

    // Метод для применения сохраненной темы
    fun applySavedTheme(context: Context) {
        // Получаем SharedPreferences для чтения
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Получаем значение темы (по умолчанию светлая)
        val isDark = prefs.getBoolean(KEY_DARK_THEME, false)
        AppCompatDelegate.setDefaultNightMode(              // Применяем тему глобально
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
