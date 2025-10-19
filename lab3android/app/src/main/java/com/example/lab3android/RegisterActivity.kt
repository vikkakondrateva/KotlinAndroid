package com.example.lab3android

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    private lateinit var database: Database

    // Поля ввода (соответствуют твоим id из XML)
    private lateinit var loginEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var surnameEditText: EditText  // SurnameEditText
    private lateinit var nameEditText: EditText     // NameEditText
    private lateinit var patronymicEditText: EditText // PatronymicEditText
    private lateinit var birthDateEditText: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var registerButton: Button
    private lateinit var avatarImage: ImageView
    private lateinit var selectAvatarButton: Button
    private var selectedAvatarResId: Int = R.drawable.avatar_default

    private val availableAvatars = listOf(
        R.drawable.avatar1,
        R.drawable.avatar2,
        R.drawable.avatar3,
        R.drawable.avatar4,
        R.drawable.avatar5,
        R.drawable.avatar_default
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        database = Database(this)

        // Находим все элементы на экране (по твоим id)
        loginEditText = findViewById(R.id.loginEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        surnameEditText = findViewById(R.id.SurnameEditText)      // Обрати внимание на большую S
        nameEditText = findViewById(R.id.NameEditText)            // Обрати внимание на большую N
        patronymicEditText = findViewById(R.id.PatronymicEditText) // Обрати внимание на большую P
        birthDateEditText = findViewById(R.id.birthDateEditText)
        genderRadioGroup = findViewById(R.id.genderRadioGroup)
        registerButton = findViewById(R.id.registerButton)
        avatarImage = findViewById(R.id.avatarImage)
        selectAvatarButton = findViewById(R.id.selectAvatarButton)
        avatarImage.setImageResource(selectedAvatarResId)


        // Обработчик кнопки регистрации
        registerButton.setOnClickListener {
            registerUser()
        }

        selectAvatarButton.setOnClickListener {
            showAvatarSelectionDialog()
        }

        avatarImage.setOnClickListener {
            showAvatarSelectionDialog()
        }
    }



    private fun showAvatarSelectionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Выберите аватар")
            .setNegativeButton("Отмена") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        val container = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        val rows = mutableListOf<LinearLayout>()
        val numberOfRows = (availableAvatars.size + 2) / 3

        for (i in 0 until numberOfRows) {
            val row = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER
            }
            rows.add(row)
            container.addView(row)
        }

        availableAvatars.forEachIndexed { index, avatarResId ->
            val rowIndex = index / 3
            val row = rows[rowIndex]

            val avatarOption = ImageView(this).apply {
                setImageResource(avatarResId)
                layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                    setMargins(16, 16, 16, 16)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setOnClickListener {
                    selectedAvatarResId = avatarResId
                    avatarImage.setImageResource(selectedAvatarResId)
                    Toast.makeText(this@RegisterActivity, "Аватар выбран", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                setBackgroundResource(android.R.drawable.btn_default)
            }

            row.addView(avatarOption)
        }

        dialog.setView(container)
        dialog.show()
    }

    private fun registerUser() {
        // Получаем данные из полей
        val login = loginEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val lastName = surnameEditText.text.toString().trim()     // Фамилия = Surname
        val firstName = nameEditText.text.toString().trim()       // Имя = Name
        val middleName = patronymicEditText.text.toString().trim() // Отчество = Patronymic
        val birthDate = birthDateEditText.text.toString().trim()

        // Получаем выбранный пол
        val selectedGenderId = genderRadioGroup.checkedRadioButtonId
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Выберите пол", Toast.LENGTH_SHORT).show()
            return
        }
        val genderRadioButton = findViewById<RadioButton>(selectedGenderId)
        val gender = genderRadioButton.text.toString()

        // Проверяем, что обязательные поля заполнены
        if (login.isEmpty() || password.isEmpty() || lastName.isEmpty() ||
            firstName.isEmpty() || birthDate.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверяем, не занят ли логин
        if (database.isLoginExists(login)) {
            Toast.makeText(this, "Пользователь с таким логином уже существует", Toast.LENGTH_SHORT).show()
            return
        }

        // Регистрируем пользователя
        val success = database.registerUser(
            login = login,
            password = password,
            lastName = lastName,
            firstName = firstName,
            middleName = if (middleName.isEmpty()) null else middleName,
            birthDate = birthDate,
            gender = gender,
            avatarPath = null // Пока без аватара
        )

        if (success) {
            Toast.makeText(this, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
            finish() // Закрываем окно регистрации
        } else {
            Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show()
        }
    }
}