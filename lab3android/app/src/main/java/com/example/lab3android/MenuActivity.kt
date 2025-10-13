package com.example.lab3android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView

class MenuActivity : Activity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        Log.d("Lifecycle", "MenuActivity - onCreate")

        val startButton = findViewById<Button>(R.id.startButton)
        val profileButton = findViewById<Button>(R.id.profileButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)
        val reportProblemButton = findViewById<Button>(R.id.reportProblemButton)
        val exitButton = findViewById<Button>(R.id.exitButton)

        val loginName = findViewById<TextView>(R.id.loginName)
        val recievedLogin = intent.getStringExtra("lоginkey")

        Log.d("HelloActivity", "Приняли $recievedLogin")
        loginName.setText(recievedLogin)
        //val recievedLogin = intent.extras?.getString("loginkey")

        //val recievedLogin = intent.getStringExtra("loginkey")

        // Старт (лог)
        startButton.setOnClickListener {
            Log.d("Menu", "Кнопка Старт нажата")
        }

        // Профиль - ProfileActivity
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
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

        // Пытаемся открыть почтовое приложение
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            // Если нет почты - предлагаем позвонить
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