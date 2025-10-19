package com.example.lab3android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity

class AdminActivity : AppCompatActivity() {

    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var backButton: Button
    private lateinit var userAdapter: UserAdapter
    private lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        Log.d("Lifecycle", "AdminActivity - onCreate")

        database = Database(this)
        // Проверка базы данных
        val userCount = database.getUsersCount()
        Log.d("AdminActivity", "В базе данных пользователей: $userCount")

        initializeViews()
        setupRecyclerView()
        loadUsers()
    }

    private fun initializeViews() {
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        backButton = findViewById(R.id.backButton)

        backButton.setOnClickListener {
            Log.d("AdminActivity", "Кнопка Назад нажата")
            finish()
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter()

        // Устанавливаем слушатель кликов
        userAdapter.setOnUserClickListener(object : UserAdapter.OnUserClickListener {
            override fun onUserClick(user: User) {
                // Клик на пользователе - открываем его профиль
                openUserProfile(user)
            }
        })

        usersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AdminActivity)
            adapter = userAdapter
            // Добавляем разделитель между элементами
            addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(
                this@AdminActivity, LinearLayoutManager.VERTICAL
            ))
        }
    }

    private fun loadUsers() {
        val users = database.getAllUsers()
        Log.d("AdminActivity", "Загружено пользователей: ${users.size}")

        // Детальная информация о каждом пользователе
        users.forEach { user ->
            Log.d("AdminActivity", "Пользователь: ${user.login}, ${user.firstName} ${user.lastName}, Админ: ${user.isAdmin}")
        }

        if (users.isEmpty()) {
            Toast.makeText(this, "Нет зарегистрированных пользователей", Toast.LENGTH_SHORT).show()
            Log.d("AdminActivity", "БД пуста - пользователей нет")
        } else {
            Toast.makeText(this, "Загружено пользователей: ${users.size}", Toast.LENGTH_SHORT).show()
        }

        userAdapter.setUsers(users)

        // Проверяем, что адаптер обновился
        Log.d("AdminActivity", "Адаптер содержит: ${userAdapter.itemCount} элементов")
    }

    private fun openUserProfile(user: User) {
        Log.d("AdminActivity", "Открываем профиль пользователя: ${user.login}")

        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("user_id", user.id)
        startActivityForResult(intent, 1) // Используем startActivityForResult для обновления списка
    }

    // Добавьте метод для обработки результата
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            // Обновляем список пользователей после редактирования
            loadUsers()
            Toast.makeText(this, "Данные пользователя обновлены", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUserActionsMenu(user: User, view: View) {
        Log.d("AdminActivity", "Показываем меню действий для: ${user.login}")

        // TODO: Реализовать PopupMenu с действиями
        // - Сделать администратором
        // - Заблокировать
        // - Удалить

        Toast.makeText(this, "Меню действий для: ${user.login}", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        Log.d("Lifecycle", "AdminActivity - onStart")
    }

    override fun onResume() {
        super.onResume()
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