package com.example.lab3android

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.ImageView

class ProfileActivity : Activity() {

    private lateinit var loginEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var patronymicEditText: EditText
    private lateinit var birthDateEditText: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var registerButton: Button
    private lateinit var selectAvatarButton: Button
    private lateinit var avatarImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        Log.d("Lifecycle", "ProfileActivity - onCreate")

        loginEditText = findViewById(R.id.loginEditText) // было logInEditText
        passwordEditText = findViewById(R.id.passwordEditText)
        surnameEditText = findViewById(R.id.SurnameEditText)
        nameEditText = findViewById(R.id.NameEditText)
        patronymicEditText = findViewById(R.id.PatronymicEditText) // было PatronymidEditText
        birthDateEditText = findViewById(R.id.birthDateEditText)
        genderRadioGroup = findViewById(R.id.genderRadioGroup) // было genderRedisGroup
        registerButton = findViewById(R.id.registerButton) // было resistorButton
        selectAvatarButton = findViewById(R.id.selectAvatarButton)
        avatarImage = findViewById(R.id.avatarImage)

        // Восстанавливаем сохраненные данные
        if (savedInstanceState != null) {
            restoreSavedData(savedInstanceState)
        }

        setupClickListeners()
    }


    private fun restoreSavedData(savedInstanceState: Bundle) {
        // Используем безопасные вызовы для избежания NPE
        savedInstanceState.getString("login")?.let { loginEditText.setText(it) }
        savedInstanceState.getString("password")?.let { passwordEditText.setText(it) }
        savedInstanceState.getString("surname")?.let { surnameEditText.setText(it) }
        savedInstanceState.getString("name")?.let { nameEditText.setText(it) }
        savedInstanceState.getString("patronymic")?.let { patronymicEditText.setText(it) }
        savedInstanceState.getString("birthDate")?.let { birthDateEditText.setText(it) }

        val savedGender = savedInstanceState.getInt("gender", -1)
        if (savedGender != -1) {
            genderRadioGroup.check(savedGender)
        }

        Log.d("ProfileActivity", getString(R.string.log_data_restored))
    }

    private fun setupClickListeners() {
        // Обработка кнопки выбора аватара
        selectAvatarButton.setOnClickListener {
            safeLog(R.string.log_avatar_button_clicked)
        }

        // Обработка клика по аватару
        avatarImage.setOnClickListener {
            safeLog(R.string.log_avatar_clicked)
        }

        // Обработка кнопки регистрации
        registerButton.setOnClickListener {
            handleRegistration()
        }
    }

    private fun handleRegistration() {
        val login = loginEditText.text.toString()
        val password = passwordEditText.text.toString()
        val surname = surnameEditText.text.toString()
        val name = nameEditText.text.toString()
        val patronymic = patronymicEditText.text.toString()
        val birthDate = birthDateEditText.text.toString()

        // Получаем выбранный пол
        val selectedGenderId = genderRadioGroup.checkedRadioButtonId
        val gender = if (selectedGenderId != -1) {
            val selectedRadio = findViewById<RadioButton>(selectedGenderId)
            selectedRadio.text.toString()
        } else {
            "Не указан"
        }

        Log.d("Registration", "Регистрация: Логин=$login, Пароль=$password, Фамилия=$surname, Имя=$name, Отчество=$patronymic, Дата=$birthDate, Пол=$gender")

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString("login", loginEditText.text.toString())
        outState.putString("password", passwordEditText.text.toString())
        outState.putString("surname", surnameEditText.text.toString())
        outState.putString("name", nameEditText.text.toString())
        outState.putString("patronymic", patronymicEditText.text.toString())
        outState.putString("birthDate", birthDateEditText.text.toString())
        outState.putInt("gender", genderRadioGroup.checkedRadioButtonId)

        safeLog(R.string.log_data_saved)
    }

    // Вспомогательная функция для безопасного логирования
    private fun safeLog(stringResId: Int) {
            Log.d("ProfileActivity", getString(stringResId))
    }

    // Методы жизненного цикла
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