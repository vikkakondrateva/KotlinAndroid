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

class ProfileActivity : AppCompatActivity() {

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
    private lateinit var makeAdminButton: Button

    private var selectedAvatarResId: Int = R.drawable.avatar_default
    private val availableAvatars = listOf(
        R.drawable.avatar1,
        R.drawable.avatar2,
        R.drawable.avatar3,
        R.drawable.avatar4,
        R.drawable.avatar5,
        R.drawable.avatar_default
    )

    private lateinit var database: Database
    private var currentUserId: Long = 0
    private var isEditMode: Boolean = false
    private var originalLogin: String = ""
    private var pendingIsAdmin: Boolean = false // Временное хранение статуса админа

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        Log.d("Lifecycle", "ProfileActivity - onCreate")

        database = Database(this)
        initializeViews()

        // Восстанавливаем сохраненные данные если есть
        if (savedInstanceState != null) {
            restoreSavedData(savedInstanceState)
        } else {
            setupUserData()
        }

        setupClickListeners()
    }

    private fun initializeViews() {
        loginEditText = findViewById(R.id.loginEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        surnameEditText = findViewById(R.id.SurnameEditText)
        nameEditText = findViewById(R.id.NameEditText)
        patronymicEditText = findViewById(R.id.PatronymicEditText)
        birthDateEditText = findViewById(R.id.birthDateEditText)
        genderRadioGroup = findViewById(R.id.genderRadioGroup)
        registerButton = findViewById(R.id.registerButton)
        selectAvatarButton = findViewById(R.id.selectAvatarButton)
        avatarImage = findViewById(R.id.avatarImage)
        makeAdminButton = findViewById(R.id.makeAdminButton)

        avatarImage = findViewById(R.id.avatarImage)
        selectAvatarButton = findViewById(R.id.selectAvatarButton)
        avatarImage.setImageResource(selectedAvatarResId)

    }

    private fun setupUserData() {
        // Получаем данные из Intent (если переходим из списка пользователей)
        val intent = intent
        if (intent.hasExtra("user_id")) {
            isEditMode = true
            currentUserId = intent.getLongExtra("user_id", 0)
            val user = database.getUserById(currentUserId)

            user?.let {
                fillUserData(it)
                registerButton.text = "Сохранить изменения"
                makeAdminButton.visibility = android.view.View.VISIBLE
                pendingIsAdmin = user.isAdmin // Инициализируем временное значение
            } ?: run {
                Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            // Режим создания нового пользователя
            registerButton.text = "Зарегистрировать"
            makeAdminButton.visibility = android.view.View.GONE
            pendingIsAdmin = false // Новые пользователи по умолчанию не админы
        }
    }

    private fun fillUserData(user: User) {
        loginEditText.setText(user.login)
        passwordEditText.setText(user.password)
        surnameEditText.setText(user.lastName)
        nameEditText.setText(user.firstName)
        patronymicEditText.setText(user.middleName ?: "")
        birthDateEditText.setText(user.birthDate)

        // Устанавливаем пол - ВАЖНО!
        when (user.gender) {
            "М" -> {
                val maleRadio = findViewById<RadioButton>(R.id.maleRadioButton)
                maleRadio.isChecked = true
            }
            "Ж" -> {
                val femaleRadio = findViewById<RadioButton>(R.id.femaleRadioButton)
                femaleRadio.isChecked = true
            }
            else -> {
                // Если пол не установлен, сбрасываем выбор
                genderRadioGroup.clearCheck()
            }
        }

        // Устанавливаем аватар
        if (user.avatarPath != null && user.avatarPath.startsWith("avatar_res_")) {
            try {
                val resId = user.avatarPath.removePrefix("avatar_res_").toInt()
                selectedAvatarResId = resId
                avatarImage.setImageResource(selectedAvatarResId)
            } catch (e: Exception) {
                // Если ошибка - оставляем аватар по умолчанию
            }
        }

        originalLogin = user.login
        pendingIsAdmin = user.isAdmin

        // Настраиваем кнопку "Сделать администратором"
        updateAdminButtonText()
    }

    private fun setupClickListeners() {
        selectAvatarButton.setOnClickListener {
            showAvatarSelectionDialog()
        }

        avatarImage.setOnClickListener {
            showAvatarSelectionDialog()
        }

        registerButton.setOnClickListener {
            if (isEditMode) {
                updateUser()
            } else {
                registerNewUser()
            }
        }

        makeAdminButton.setOnClickListener {
            // Только меняем текст кнопки, НЕ сохраняем в БД
            pendingIsAdmin = !pendingIsAdmin
            updateAdminButtonText()
            Toast.makeText(this,
                if (pendingIsAdmin) "Пользователь будет администратором после сохранения"
                else "Права администратора будут убраны после сохранения",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateAdminButtonText() {
        if (pendingIsAdmin) {
            makeAdminButton.text = "Убрать права администратора"
        } else {
            makeAdminButton.text = "Сделать администратором"
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
                    Toast.makeText(this@ProfileActivity, "Аватар выбран", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                setBackgroundResource(android.R.drawable.btn_default)
            }

            row.addView(avatarOption)
        }

        dialog.setView(container)
        dialog.show()
    }

    private fun registerNewUser() {
        val userData = getUserDataFromForm()
        if (userData == null) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show()
            return
        }

        val (login, password, lastName, firstName, middleName, birthDate, gender) = userData
        val avatarPath = "avatar_res_$selectedAvatarResId"

        // Проверяем, не занят ли логин
        if (database.isLoginExists(login)) {
            Toast.makeText(this, "Пользователь с таким логином уже существует", Toast.LENGTH_SHORT).show()
            return
        }

        val success = database.registerUser(
            login = login,
            password = password,
            lastName = lastName,
            firstName = firstName,
            middleName = middleName,
            birthDate = birthDate,
            gender = gender,
            avatarPath = avatarPath
        )

        if (success) {
            Toast.makeText(this, "Пользователь зарегистрирован!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            Toast.makeText(this, "Ошибка регистрации", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUser() {
        val userData = getUserDataFromForm()
        if (userData == null) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show()
            return
        }

        val (login, password, lastName, firstName, middleName, birthDate, gender) = userData
        val avatarPath = "avatar_res_$selectedAvatarResId"

        // Проверяем, не занят ли логин другим пользователем
        if (login != originalLogin && database.isLoginExists(login)) {
            Toast.makeText(this, "Пользователь с таким логином уже существует", Toast.LENGTH_SHORT).show()
            return
        }

        val success = database.updateUser(
            userId = currentUserId,
            login = login,
            password = password,
            lastName = lastName,
            firstName = firstName,
            middleName = middleName,
            birthDate = birthDate,
            gender = gender,
            avatarPath = avatarPath,
            isAdmin = pendingIsAdmin // Используем временное значение
        )

        if (success) {
            Toast.makeText(this, "Данные пользователя обновлены!", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Ошибка обновления данных", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getUserDataFromForm(): UserFormData? {
        val login = loginEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()
        val lastName = surnameEditText.text.toString().trim()
        val firstName = nameEditText.text.toString().trim()
        val middleName = patronymicEditText.text.toString().trim()
        val birthDate = birthDateEditText.text.toString().trim()

        val selectedGenderId = genderRadioGroup.checkedRadioButtonId
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Выберите пол", Toast.LENGTH_SHORT).show()
            return null
        }
        val genderRadioButton = findViewById<RadioButton>(selectedGenderId)
        val gender = genderRadioButton.text.toString()

        if (login.isEmpty() || password.isEmpty() || lastName.isEmpty() ||
            firstName.isEmpty() || birthDate.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show()
            return null
        }

        return UserFormData(login, password, lastName, firstName, middleName, birthDate, gender)
    }

    // Вспомогательный data class для данных формы
    private data class UserFormData(
        val login: String,
        val password: String,
        val lastName: String,
        val firstName: String,
        val middleName: String?,
        val birthDate: String,
        val gender: String
    )

    private fun restoreSavedData(savedInstanceState: Bundle) {
        savedInstanceState.getString("login")?.let { loginEditText.setText(it) }
        savedInstanceState.getString("password")?.let { passwordEditText.setText(it) }
        savedInstanceState.getString("surname")?.let { surnameEditText.setText(it) }
        savedInstanceState.getString("name")?.let { nameEditText.setText(it) }
        savedInstanceState.getString("patronymic")?.let { patronymicEditText.setText(it) }
        savedInstanceState.getString("birthDate")?.let { birthDateEditText.setText(it) }

        // Восстанавливаем выбранный пол
        val savedGenderId = savedInstanceState.getInt("gender", -1)
        if (savedGenderId != -1) {
            genderRadioGroup.check(savedGenderId)
        }

        val savedAvatarResId = savedInstanceState.getInt("avatarResId", -1)
        if (savedAvatarResId != -1) {
            selectedAvatarResId = savedAvatarResId
            avatarImage.setImageResource(selectedAvatarResId)
        }

        // Восстанавливаем временный статус админа
        pendingIsAdmin = savedInstanceState.getBoolean("pendingIsAdmin", false)
        updateAdminButtonText()

        Log.d("ProfileActivity", "Данные восстановлены")
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
        outState.putInt("avatarResId", selectedAvatarResId)
        outState.putBoolean("pendingIsAdmin", pendingIsAdmin) // Сохраняем временный статус

        Log.d("ProfileActivity", "Данные сохранены")
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