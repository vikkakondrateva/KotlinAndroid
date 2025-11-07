package com.example.lab3android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.content.Context          // Класс для доступа к ресурсам приложения, SharedPreferences
import androidx.appcompat.app.AppCompatActivity     // Базовый класс активности с поддержкой старых версий
import androidx.appcompat.app.AppCompatDelegate     // Класс для управления темами (светлая/темная)
import com.example.lab3android.databinding.ActivityHelloBinding

class HelloActivity : AppCompatActivity() {

    //private lateinit var loginEditText: EditText
    //private lateinit var passwordEditText: EditText
    private lateinit var binding: ActivityHelloBinding
    private lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {

        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityHelloBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("Lifecycle", "HelloActivity - onCreate")

        // Инициализируем базу данных
        database = Database(this)

        //loginEditText = findViewById(R.id.loginEditText)
        //passwordEditText = findViewById(R.id.passwordEditText)
        //val loginButton = findViewById<Button>(R.id.loginButton)
        //val registerButton = findViewById<Button>(R.id.registerButton)
        //val exitButton = findViewById<Button>(R.id.exitButton)

        // Восстанавливаем сохраненные данные
        if (savedInstanceState != null) {
            val savedLogin = savedInstanceState.getString("login")
            val savedPassword = savedInstanceState.getString("password")

            // Устанавливаем сохраненные значения в поля ввода
            binding.loginEditText.setText(savedLogin)
            binding.passwordEditText.setText(savedPassword)
            Log.d("HelloActivity", getString(R.string.log_data_restored))
        }

        binding.loginButton.setOnClickListener {
            Log.d("HelloActivity", "Кнопка Вход нажата")
            handleLogin()       // Вызов метода обработки входа
        }

        binding.registerButton.setOnClickListener {
            Log.d("HelloActivity", "Кнопка Регистрация нажата")
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)        // Запуск активности регистрации
        }

        binding.exitButton.setOnClickListener {
            Log.d("HelloActivity", "Кнопка Выход нажата")
            finish()
        }
    }

    // Метод обработки входа пользователя
    private fun handleLogin() {
        // Получаем логин и пароль из поля ввода и удаляем пробелы по краям
        val login = binding.loginEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        // Проверяем что поля заполнены
        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            // Проверяем пользователя в БД ← Теперь в фоновом потоке!
            val user = database.getUser(login, password)

            runOnUiThread {
                if (user != null) {
                    // Получаем тему пользователя из БД (быстрая операция, можно в UI потоке)
                    val theme = database.getUserTheme(user.login)
                    val isDark = theme == "dark"

                    // Сохраняем текущего пользователя и тему в SharedPreferences
                    val prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE).edit()
                    prefs.putBoolean("dark_theme", isDark)
                    prefs.putString("current_user", user.login)
                    prefs.apply()

                    // Применяем тему глобально
                    AppCompatDelegate.setDefaultNightMode(
                        if (isDark) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_NO
                    )

                    // Успешный вход
                    Log.d("HelloActivity", "Успешный вход пользователя: ${user.login}, Админ: ${user.isAdmin}")
                    Toast.makeText(this, "Добро пожаловать, ${user.firstName}!", Toast.LENGTH_SHORT).show()

                    // Переходим в MenuActivity и передаем данные пользователя
                    val intent = Intent(this, MenuActivity::class.java)
                    intent.putExtra("user_login", user.login)
                    intent.putExtra("user_name", "${user.firstName}")
                    intent.putExtra("is_admin", user.isAdmin)
                    startActivity(intent)

                } else {
                    // Неверные данные
                    Log.d("HelloActivity", "Неверный логин или пароль для: $login")
                    Toast.makeText(this, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Вызов родительского метода
        super.onSaveInstanceState(outState)

        // Получаем текущие значения из полей ввода
        val login = binding.loginEditText.text.toString()
        val password = binding.passwordEditText.text.toString()

        // Сохраняем логин и пароль в Bundle для восстановления
        outState.putString("login", login)
        outState.putString("password", password)
        Log.d("HelloActivity", "Данные сохранены: login=$login")
    }

    override fun onStart() {
        super.onStart()
        Log.d("Lifecycle", "HelloActivity - onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Lifecycle", "HelloActivity - onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("Lifecycle", "HelloActivity - onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Lifecycle", "HelloActivity - onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Lifecycle", "HelloActivity - onDestroy")
    }
}