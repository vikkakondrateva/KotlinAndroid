package com.example.lab3android

import android.app.Activity
import android.content.Intent
import android.net.Uri          // Класс для работы с URI (телефонные номера, ссылки)
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lab3android.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    //private lateinit var adminButton: Button
    //private lateinit var loginName: TextView
    private lateinit var binding: ActivityMenuBinding
    private lateinit var database: Database  // Объявляем переменную

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_menu)
        Log.d("Lifecycle", "MenuActivity - onCreate")

        database = Database(this)

        //val startButton = findViewById<Button>(R.id.startButton)
        //val profileButton = findViewById<Button>(R.id.profileButton)
        //val settingsButton = findViewById<Button>(R.id.settingsButton)
        //val reportProblemButton = findViewById<Button>(R.id.reportProblemButton)
        //val exitButton = findViewById<Button>(R.id.exitButton)
        //adminButton = findViewById<Button>(R.id.adminButton)
        //loginName = findViewById<TextView>(R.id.loginName)

        // Получаем данные пользователя
        val userLogin = intent.getStringExtra("user_login")
        val userName = intent.getStringExtra("user_name")
        val isAdmin = intent.getBooleanExtra("is_admin", false)

        Log.d("MenuActivity", "Пользователь: $userLogin, Админ: $isAdmin")

        // Отображаем имя пользователя
        if (!userName.isNullOrEmpty()) {
            binding.loginName.text = "Добро пожаловать,\n$userName!"
        } else if (!userLogin.isNullOrEmpty()) {
            binding.loginName.text = "Добро пожаловать,\n$userLogin!"
        }

        // Показываем кнопку администрирования только администраторам
        if (isAdmin) {
            binding.adminButton.visibility = android.view.View.VISIBLE
            Log.d("MenuActivity", "Пользователь является администратором")
        } else {
            binding.adminButton.visibility = android.view.View.GONE
            Log.d("MenuActivity", "Пользователь обычный")
        }

        // Старт (лог)
        binding.startButton.setOnClickListener {
            Log.d("Menu", "Кнопка Старт нажата")
        }

        // Профиль - ProfileActivity
        binding.profileButton.setOnClickListener {
            Log.d("MenuActivity", "Кнопка Профиль нажата")

            // Получаем логин текущего пользователя
            val userLogin = intent.getStringExtra("user_login")
            if (!userLogin.isNullOrEmpty()) {
                Thread {
                    // Находим пользователя по логину чтобы получить его ID
                    val currentUser = database.getUserByLogin(userLogin)

                    // ⬇⬇⬇ ОБНОВЛЕНИЕ UI В ГЛАВНОМ ПОТОКЕ ⬇⬇⬇
                    runOnUiThread {
                        val profileIntent = Intent(this, ProfileActivity::class.java)
                        if (currentUser != null) {
                            profileIntent.putExtra("user_id", currentUser.id)
                            profileIntent.putExtra("is_admin", currentUser.isAdmin)  // ← Исправлено: было currentUser.id, теперь currentUser.isAdmin
                            Log.d("MenuActivity", "Передаем ID пользователя: ${currentUser.id}")
                            startActivity(profileIntent)
                        } else {
                            Log.e("MenuActivity", "Не удалось найти пользователя с логином: $userLogin")
                            Toast.makeText(this, "Ошибка: не найден пользователь в БД", Toast.LENGTH_SHORT).show()
                        }
                    }
                    // ⬆⬆⬆ ОБНОВЛЕНИЕ UI В ГЛАВНОМ ПОТОКЕ ⬆⬆⬆
                }.start()
            } else {
                Toast.makeText(this, "Ошибка: не найден текущий пользователь", Toast.LENGTH_SHORT).show()
            }
        }

        // Администрирование - AdminActivity
        binding.adminButton.setOnClickListener {
            Log.d("Menu", "Кнопка Администрирование нажата")
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }

        // Настройки - SettingsActivity
        binding.settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Сообщить о проблеме - почта или звонок
        binding.reportProblemButton.setOnClickListener {
            showReportProblemDialog()
        }

        // Выход - закрываем MenuActivity
        binding.exitButton.setOnClickListener {
            finish()
        }
    }

    // Метод для обработки сообщений о проблемах
    private fun showReportProblemDialog() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("vikka_kondrateva@mail.ru"))
            putExtra(Intent.EXTRA_SUBJECT, "Проблема в приложении AVA")
            putExtra(Intent.EXTRA_TEXT, "Спасите, помогите!!! У меня проблемки(((")
        }

        if (intent.resolveActivity(packageManager) != null) {        // Проверяем, есть ли приложение которое может обработать Intent отправки email
            startActivity(intent)
        } else {
            val callIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:+79513576242")
            }
            if (callIntent.resolveActivity(packageManager) != null) {   // Проверяем, есть ли приложение для звонков
                startActivity(callIntent)
            } else {
                Log.e("Menu", "Нет приложения для почты или звонков")
            }
        }
    }
}