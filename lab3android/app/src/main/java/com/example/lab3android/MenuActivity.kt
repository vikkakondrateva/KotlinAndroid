package com.example.lab3android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    private lateinit var adminButton: Button
    private lateinit var loginName: TextView
    private lateinit var database: Database  // Объявляем переменную

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        Log.d("Lifecycle", "MenuActivity - onCreate")

        // ИНИЦИАЛИЗИРУЕМ БАЗУ ДАННЫХ
        database = Database(this)  // Вот эта строка была пропущена!

        val startButton = findViewById<Button>(R.id.startButton)
        val profileButton = findViewById<Button>(R.id.profileButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val reportProblemButton = findViewById<Button>(R.id.reportProblemButton)
        val exitButton = findViewById<Button>(R.id.exitButton)
        adminButton = findViewById<Button>(R.id.adminButton)
        loginName = findViewById<TextView>(R.id.loginName)

        // Получаем данные пользователя
        val userLogin = intent.getStringExtra("user_login")
        val userName = intent.getStringExtra("user_name")
        val isAdmin = intent.getBooleanExtra("is_admin", false)

        Log.d("MenuActivity", "Пользователь: $userLogin, Админ: $isAdmin")

        // Отображаем имя пользователя
        if (!userName.isNullOrEmpty()) {
            loginName.text = "Добро пожаловать,\n$userName!"
        } else if (!userLogin.isNullOrEmpty()) {
            loginName.text = "Добро пожаловать,\n$userLogin!"
        }

        // Показываем кнопку администрирования только администраторам
        if (isAdmin) {
            adminButton.visibility = android.view.View.VISIBLE
            Log.d("MenuActivity", "Пользователь является администратором")
        } else {
            adminButton.visibility = android.view.View.GONE
            Log.d("MenuActivity", "Пользователь обычный")
        }

        // Старт (лог)
        startButton.setOnClickListener {
            Log.d("Menu", "Кнопка Старт нажата")
        }

        // Профиль - ProfileActivity
        profileButton.setOnClickListener {
            Log.d("MenuActivity", "Кнопка Профиль нажата")

            // Получаем логин текущего пользователя
            val userLogin = intent.getStringExtra("user_login")
            if (!userLogin.isNullOrEmpty()) {
                // Находим пользователя по логину чтобы получить его ID
                val currentUser = database.getUserByLogin(userLogin)

                val profileIntent = Intent(this, ProfileActivity::class.java)
                if (currentUser != null) {
                    profileIntent.putExtra("user_id", currentUser.id)
                    Log.d("MenuActivity", "Передаем ID пользователя: ${currentUser.id}")
                    startActivity(profileIntent)
                } else {
                    Log.e("MenuActivity", "Не удалось найти пользователя с логином: $userLogin")
                    Toast.makeText(this, "Ошибка: не найден пользователь в БД", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Ошибка: не найден текущий пользователь", Toast.LENGTH_SHORT).show()
            }
        }

        // Администрирование - AdminActivity
        adminButton.setOnClickListener {
            Log.d("Menu", "Кнопка Администрирование нажата")
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }

        // Настройки - SettingsActivity
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Сообщить о проблеме - почта или звонок
        reportProblemButton.setOnClickListener {
            showReportProblemDialog()
        }

        // Выход - закрываем MenuActivity
        exitButton.setOnClickListener {
            finish()
        }
    }

    private fun showReportProblemDialog() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("vikka_kondrateva@mail.ru"))
            putExtra(Intent.EXTRA_SUBJECT, "Проблема в приложении AVA")
            putExtra(Intent.EXTRA_TEXT, "Спасите, помогите!!! У меня проблемки(((")
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+79513576242")
            }
            if (callIntent.resolveActivity(packageManager) != null) {
                startActivity(callIntent)
            } else {
                Log.e("Menu", "Нет приложения для почты или звонков")
            }
        }
    }
}