package com.example.lab3android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager // Менеджер компоновки для линейного списка
import androidx.recyclerview.widget.RecyclerView        // Компонент для отображения прокручиваемых списков
import androidx.appcompat.app.AppCompatActivity         // Базовый класс активности с поддержкой старых версий
import com.example.lab3android.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    //private lateinit var usersRecyclerView: RecyclerView    // Список пользователей
    //private lateinit var backButton: Button
    private lateinit var userAdapter: UserAdapter
    private lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)    // Применение сохраненной темы перед созданием активности
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d("Lifecycle", "AdminActivity - onCreate")

        database = Database(this)
        val userCount = database.getUsersCount()    // Получение количества пользователей в базе данных
        Log.d("AdminActivity", "В базе данных пользователей: $userCount")

        initializeViews()       // Инициализация View элементов
        setupRecyclerView()     // Настройка списка пользоватеоей
        loadUsers()              // Загрузка пользователей из базы данных
    }

    private fun initializeViews() {
        binding.backButton.setOnClickListener {
            Log.d("AdminActivity", "Кнопка Назад нажата")
            finish()
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter()

        // Устанавливаем слушатель кликов на элементы списка
        userAdapter.setOnUserClickListener(object : UserAdapter.OnUserClickListener {
            override fun onUserClick(user: User) {
                // Клик на пользователе - открываем его профиль
                openUserProfile(user)
            }
        })

        binding.usersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AdminActivity)
            adapter = userAdapter
            addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(
                this@AdminActivity, LinearLayoutManager.VERTICAL
            ))
        }
    }

    private fun loadUsers() {
        Thread {
            val users = database.getAllUsers()      // ← Теперь в фоновом потоке!
            Log.d("AdminActivity", "Загружено пользователей: ${users.size}")

            runOnUiThread {
                if (users.isEmpty()) {
                    Toast.makeText(this, "Нет зарегистрированных пользователей", Toast.LENGTH_SHORT).show()
                    Log.d("AdminActivity", "БД пуста - пользователей нет")
                } else {
                    Toast.makeText(this, "Загружено пользователей: ${users.size}", Toast.LENGTH_SHORT).show()
                }

                userAdapter.setUsers(users)  // ← Теперь в UI потоке!
                Log.d("AdminActivity", "Адаптер содержит: ${userAdapter.itemCount} элементов")
            }
        }.start()
    }

    private fun openUserProfile(user: User) {
        Log.d("AdminActivity", "Открываем профиль пользователя: ${user.login}")

        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("user_id", user.id)
        startActivityForResult(intent, 1) // Используем startActivityForResult для обновления списка
    }

    // Метод для обработки результата от дочерней активности
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)        // Вызов родительского метода
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // Обновляем список пользователей после редактирования
            loadUsers()
            Toast.makeText(this, "Данные пользователя обновлены", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Lifecycle", "AdminActivity - onStart")
    }

    override fun onResume() {
        super.onResume()
        // Повторное применение темы на случай изменений
        ThemeUtils.applySavedTheme(this)
        delegate.applyDayNight()
        Log.d("Lifecycle", "AdminActivity - onResume")
        // Обновляем список при возвращении на экран
        loadUsers()
    }

    override fun onPause() {
        super.onPause()
        Log.d("Lifecycle", "AdminActivity - onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Lifecycle", "AdminActivity - onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Lifecycle", "AdminActivity - onDestroy")
    }
}