package com.example.lab3android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText


class HelloActivity : Activity() {

    private lateinit var loginEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello)
        Log.d("Lifecycle", "HelloActivity - onCreate")

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
            val intent = Intent(this, MenuActivity::class.java)
            val aboba = loginEditText.text.toString()
            intent.putExtra("lоginkey",aboba)
            Log.d("HelloActivity", "Приняли $aboba")
            startActivity(intent)
        }

        registerButton.setOnClickListener {
            Log.d("HelloActivity", "Кнопка Регистрация нажата")
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        exitButton.setOnClickListener {
            Log.d("HelloActivity", "Кнопка Выход нажата")
            finish()
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
        Log.d("Lifecycle", "ProfileActivity - onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Lifecycle", "ProfileActivity - onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("Lifecycle", "ProfileActivity - onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Lifecycle", "ProfileActivity - onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Lifecycle", "ProfileActivity - onDestroy")
    }
}