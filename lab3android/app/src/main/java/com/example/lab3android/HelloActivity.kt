package com.example.lab3android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate



class HelloActivity : AppCompatActivity() {

    private lateinit var loginEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val isDark = sharedPref.getBoolean("dark_theme", false)

        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello)
        Log.d("Lifecycle", "HelloActivity - onCreate")

        // Инициализируем базу данных
        database = Database(this)

        loginEditText = findViewById(R.id.loginEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val exitButton = findViewById<Button>(R.id.exitButton)

        // Восстанавливаем сохраненные данные
        if (savedInstanceState != null) {
            val savedLogin = savedInstanceState.getString("login")
            val savedPassword = savedInstanceState.getString("password")

            loginEditText.setText(savedLogin)
            passwordEditText.setText(savedPassword)
            Log.d("HelloActivity", getString(R.string.log_data_restored))
        }

        loginButton.setOnClickListener {
            Log.d("HelloActivity", "Кнопка Вход нажата")
            handleLogin()
        }

        registerButton.setOnClickListener {
            Log.d("HelloActivity", "Кнопка Регистрация нажата")
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        exitButton.setOnClickListener {
            Log.d("HelloActivity", "Кнопка Выход нажата")
            finish()
        }
    }

    private fun handleLogin() {
        val login = loginEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Проверяем что поля заполнены
        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Введите логин и пароль", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверяем пользователя в БД
        val user = database.getUser(login, password)

        if (user != null) {
            // 1️⃣ Получаем тему пользователя из БД
            val theme = database.getUserTheme(user.login)
            val isDark = theme == "dark"

            // 2️⃣ Сохраняем текущего пользователя и тему в SharedPreferences
            val prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE).edit()
            prefs.putBoolean("dark_theme", isDark)
            prefs.putString("current_user", user.login)
            prefs.apply()

            // 3️⃣ Применяем тему глобально
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val login = loginEditText.text.toString()
        val password = passwordEditText.text.toString()

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