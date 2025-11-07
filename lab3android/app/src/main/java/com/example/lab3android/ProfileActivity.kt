package com.example.lab3android

import android.app.Activity
import android.app.AlertDialog      // Класс для создания диалоговых окон
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.lab3android.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    //private lateinit var loginEditText: EditText
    //private lateinit var passwordEditText: EditText
    //private lateinit var surnameEditText: EditText
    //private lateinit var nameEditText: EditText
    //private lateinit var patronymicEditText: EditText
    //private lateinit var birthDateEditText: EditText
    //private lateinit var genderRadioGroup: RadioGroup
    //private lateinit var registerButton: Button
    //private lateinit var selectAvatarButton: Button
    //private lateinit var avatarImage: ImageView
    //private lateinit var makeAdminButton: Button

    private lateinit var binding: ActivityProfileBinding
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
    private var currentUserId: Long = 0     // ID текущего пользователя (0 для нового)
    private var originalLogin: String = ""  // Оригинальный логин (для проверки изменений)
    private var pendingIsAdmin: Boolean = false // Временное хранение статуса админа

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //setContentView(R.layout.activity_profile)
        Log.d("Lifecycle", "ProfileActivity - onCreate")

        database = Database(this)
        initializeViews()                    // Инициализация View элементов

        // Восстанавливаем сохраненные данные если есть (при повороте экрана)
        if (savedInstanceState != null) {
            restoreSavedData(savedInstanceState)
        } else {
            setupUserData()                 // Иначе загружаем данные пользователя из Intent
        }

        setupClickListeners()
    }

    private fun initializeViews() {
        //loginEditText = findViewById(R.id.loginEditText)
        //passwordEditText = findViewById(R.id.passwordEditText)
        //surnameEditText = findViewById(R.id.SurnameEditText)
        //nameEditText = findViewById(R.id.NameEditText)
        //patronymicEditText = findViewById(R.id.PatronymicEditText)
        //birthDateEditText = findViewById(R.id.birthDateEditText)
        //genderRadioGroup = findViewById(R.id.genderRadioGroup)
        //registerButton = findViewById(R.id.registerButton)
        //selectAvatarButton = findViewById(R.id.selectAvatarButton)
        //avatarImage = findViewById(R.id.avatarImage)
        //makeAdminButton = findViewById(R.id.makeAdminButton)
        // Устанавливаем аватар по умолчанию
        binding.avatarImage.setImageResource(selectedAvatarResId)

    }

    // Метод для настройки данных пользователя
    private fun setupUserData() {
        // ProfileActivity всегда ожидает user_id для редактирования
        if (!intent.hasExtra("user_id")) {
            Toast.makeText(this, "Ошибка: не передан ID пользователя", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentUserId = intent.getLongExtra("user_id", 0)       // Получает значение, которое было передано в эту Activity через Intent

        Thread {
            val user = database.getUserById(currentUserId)
            runOnUiThread {
                user?.let {
                    fillUserData(it)
                    if (!intent.hasExtra("is_admin")) {
                        binding.makeAdminButton.visibility = android.view.View.VISIBLE
                    }
                    pendingIsAdmin = user.isAdmin
                } ?: run {
                    Toast.makeText(this, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }.start()
    }

    // Метод для заполнения формы данными пользователя
    private fun fillUserData(user: User) {
        binding.loginEditText.setText(user.login)           // Заполняем поля данными из объекта User
        binding.passwordEditText.setText(user.password)
        binding.SurnameEditText.setText(user.lastName)
        binding.NameEditText.setText(user.firstName)
        binding.PatronymicEditText.setText(user.middleName ?: "")
        binding.birthDateEditText.setText(user.birthDate)

        // Устанавливаем пол
        when (user.gender) {
            "М" -> { binding.maleRadioButton.isChecked = true
                //val maleRadio = findViewById<RadioButton>(R.id.maleRadioButton)
                //maleRadio.isChecked = true
            }
            "Ж" -> {
                //val femaleRadio = findViewById<RadioButton>(R.id.femaleRadioButton)
                //femaleRadio.isChecked = true
            }
            else -> { binding.femaleRadioButton.isChecked = true
                // Если пол не установлен, сбрасываем выбор
                binding.genderRadioGroup.clearCheck()
            }
        }

        // Устанавливаем аватар
        if (user.avatarPath != null && user.avatarPath.startsWith("avatar_res_")) {
            try {
                // Извлекаем ID ресурса из строки (формат: "avatar_res_123456")
                val resId = user.avatarPath.removePrefix("avatar_res_").toInt()
                selectedAvatarResId = resId
                binding.avatarImage.setImageResource(selectedAvatarResId)       // Устанавливаем изображение аватара
            } catch (e: Exception) {
                // Если ошибка - оставляем аватар по умолчанию
            }
        }

        originalLogin = user.login       // Сохраняем оригинальный логин для проверки изменений
        pendingIsAdmin = user.isAdmin     // Сохраняем текущий статус администратора

        // Настраиваем кнопку "Сделать администратором"
        updateAdminButtonText()
    }

    private fun setupClickListeners() {
        binding.selectAvatarButton.setOnClickListener {
            showAvatarSelectionDialog()
        }

        binding.avatarImage.setOnClickListener {
            showAvatarSelectionDialog()
        }

        binding.registerButton.setOnClickListener {
                updateUser()
        }

        binding.makeAdminButton.setOnClickListener {
            // Только меняем текст кнопки, не сохраняем в БД
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
            binding.makeAdminButton.text = "Убрать права администратора"
        } else {
            binding.makeAdminButton.text = "Сделать администратором"
        }
    }

    // Метод для показа диалога выбора аватара
    private fun showAvatarSelectionDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle("Выберите аватар")
            .setNegativeButton("Отмена") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()               // Создаем диалог

        val container = LinearLayout(this).apply {      // Создаем контейнер для аватаров
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
        }

        // Создаем строки для сетки аватаров (3 в строке)
        val rows = mutableListOf<LinearLayout>()
        val numberOfRows = (availableAvatars.size + 2) / 3

        // Создаем нужное количество строк
        for (i in 0 until numberOfRows) {
            val row = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL       // Горизонтальное расположение в строке
                gravity = android.view.Gravity.CENTER
            }
            rows.add(row)                                   // Добавляем строку в список
            container.addView(row)                   // Добавляем строку в контейнер
        }

        // Добавляем аватары в строки
        availableAvatars.forEachIndexed { index, avatarResId ->
            val rowIndex = index / 3                        // Определяем номер строки
            val row = rows[rowIndex]                        // Получаем соответствующую строку

            val avatarOption = ImageView(this).apply {   // Создаем ImageView для аватара
                setImageResource(avatarResId)                // Устанавливаем изображение аватара
                layoutParams = LinearLayout.LayoutParams(120, 120).apply {
                    setMargins(16, 16, 16, 16)   // Отступы вокруг аватара
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setOnClickListener {        // При клике на аватар выбираем его
                    selectedAvatarResId = avatarResId
                    binding.avatarImage.setImageResource(selectedAvatarResId)   // Обновляем основной аватар
                    Toast.makeText(this@ProfileActivity, "Аватар выбран", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()        // Закрываем диалог
                }
                setBackgroundResource(android.R.drawable.btn_default)   // Фон кнопки
            }

            row.addView(avatarOption)       // Добавляем аватар в строку
        }

        dialog.setView(container)                   // Устанавливаем контейнер в диалог
        dialog.show()                               // Показываем диалог
    }

    // Метод для обновления данных пользователя
    private fun updateUser() {
        val userData = getUserDataFromForm()        // Получаем данные из формы
        if (userData == null) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show()
            return
        }

        val (login, password, lastName, firstName, middleName, birthDate, gender) = userData        // Деструктурируем данные из формы
        val avatarPath = "avatar_res_$selectedAvatarResId"

        // Проверяем, не занят ли логин другим пользователем
        Thread {
            val loginExists = if (login != originalLogin) database.isLoginExists(login) else false

            runOnUiThread {
                if (loginExists) {
                    Toast.makeText(this, "Пользователь с таким логином уже существует", Toast.LENGTH_SHORT).show()
                    return@runOnUiThread
                }

                Thread {
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
                        isAdmin = pendingIsAdmin
                    )

                    runOnUiThread {
                        if (success) {
                            Toast.makeText(this, "Данные пользователя обновлены!", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        } else {
                            Toast.makeText(this, "Ошибка обновления данных", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            }
        }.start()
    }

    // Метод для получения данных из формы
    private fun getUserDataFromForm(): UserFormData? {   // Получаем и очищаем данные из полей ввода
        val login = binding.loginEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val lastName = binding.SurnameEditText.text.toString().trim()
        val firstName = binding.NameEditText.text.toString().trim()
        val middleName = binding.PatronymicEditText.text.toString().trim()
        val birthDate = binding.birthDateEditText.text.toString().trim()

        // Проверяем выбран ли пол
        val selectedGenderId = binding.genderRadioGroup.checkedRadioButtonId
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Выберите пол", Toast.LENGTH_SHORT).show()
            return null
        }
        // Получаем текст выбранной радиокнопки
        val genderRadioButton = findViewById<RadioButton>(selectedGenderId)
        val gender = genderRadioButton.text.toString()

        // Проверяем обязательные поля
        if (login.isEmpty() || password.isEmpty() || lastName.isEmpty() ||
            firstName.isEmpty() || birthDate.isEmpty()) {
            Toast.makeText(this, "Заполните все обязательные поля", Toast.LENGTH_SHORT).show()
            return null
        }

        // Возвращаем данные в виде объекта UserFormData
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

    // Метод для восстановления сохраненных данных (при повороте экрана)
    private fun restoreSavedData(savedInstanceState: Bundle) {
        savedInstanceState.getString("login")?.let { binding.loginEditText.setText(it) }
        savedInstanceState.getString("password")?.let { binding.passwordEditText.setText(it) }
        savedInstanceState.getString("surname")?.let { binding.SurnameEditText.setText(it) }
        savedInstanceState.getString("name")?.let { binding.NameEditText.setText(it) }
        savedInstanceState.getString("patronymic")?.let { binding.PatronymicEditText.setText(it) }
        savedInstanceState.getString("birthDate")?.let { binding.birthDateEditText.setText(it) }

        // Восстанавливаем выбранный пол
        val savedGenderId = savedInstanceState.getInt("gender", -1)
        if (savedGenderId != -1) {
            binding.genderRadioGroup.check(savedGenderId)
        }

        // Восстанавливаем выбранный аватар
        val savedAvatarResId = savedInstanceState.getInt("avatarResId", -1)
        if (savedAvatarResId != -1) {
            selectedAvatarResId = savedAvatarResId
            binding.avatarImage.setImageResource(selectedAvatarResId)
        }

        // Восстанавливаем временный статус админа
        pendingIsAdmin = savedInstanceState.getBoolean("pendingIsAdmin", false)
        updateAdminButtonText()

        Log.d("ProfileActivity", "Данные восстановлены")
    }

    // Метод для сохранения состояния активности
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Сохраняем текстовые поля в Bundle
        outState.putString("login", binding.loginEditText.text.toString())
        outState.putString("password", binding.passwordEditText.text.toString())
        outState.putString("surname", binding.SurnameEditText.text.toString())
        outState.putString("name", binding.NameEditText.text.toString())
        outState.putString("patronymic", binding.PatronymicEditText.text.toString())
        outState.putString("birthDate", binding.birthDateEditText.text.toString())
        outState.putInt("gender", binding.genderRadioGroup.checkedRadioButtonId)
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