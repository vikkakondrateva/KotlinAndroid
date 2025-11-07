package com.example.lab3android

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.lab3android.databinding.ActivityPlayBinding

class PlayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayBinding

    // Игровое поле 3x3
    private val board = Array(3) { Array(3) { "" } }
    private var currentPlayer = "PLAYER1" // PLAYER1 ходит первым
    private var gameActive = true

    // ID ресурсов для аватаров
    private var player1Avatar = R.drawable.avatar_default
    private var player2Avatar = R.drawable.avanew

    private lateinit var database: Database
    private var currentUserName: String = "Игрок 1"
    private var currentUserAvatar: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeUtils.applySavedTheme(this)
        super.onCreate(savedInstanceState)

        binding = ActivityPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = Database(this)
        loadCurrentUserData()
        initializeGame()
        setupClickListeners()
    }

    private fun loadCurrentUserData() {
        Thread {
            // Получаем логин текущего пользователя из SharedPreferences
            val sharedPref = getSharedPreferences("AppSettings", MODE_PRIVATE)
            val userLogin = sharedPref.getString("current_user", null)

            if (userLogin != null) {
                val user = database.getUserByLogin(userLogin)
                user?.let {
                    currentUserName = it.firstName // Используем имя пользователя
                    currentUserAvatar = it.avatarPath
                    // Если у пользователя есть свой аватар, используем его для первого игрока
                    if (it.avatarPath != null && it.avatarPath.startsWith("avatar_res_")) {
                        try {
                            val resId = it.avatarPath.removePrefix("avatar_res_").toInt()
                            player1Avatar = resId
                        } catch (e: Exception) {
                            // Если ошибка - оставляем аватар по умолчанию
                        }
                    }
                }
            }

            runOnUiThread {
                updateStatusText()
            }
        }.start()
    }

    private fun initializeGame() {
        // Очищаем поле
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                board[i][j] = ""
            }
        }

        currentPlayer = "PLAYER1"
        gameActive = true

        // Обновляем статус с именами игроков
        updateStatusText()

        // Очищаем ImageView
        val imageViews = arrayOf(
            binding.button00, binding.button01, binding.button02,
            binding.button10, binding.button11, binding.button12,
            binding.button20, binding.button21, binding.button22
        )

        imageViews.forEach { imageView ->
            imageView.setImageDrawable(null) // Убираем картинку
            imageView.background = ContextCompat.getDrawable(this, R.color.itemBackgroundColor)
            imageView.isEnabled = true
            imageView.isClickable = true
            imageView.isFocusable = true
        }
    }

    private fun updateStatusText() {
        val playerText = if (currentPlayer == "PLAYER1") {
            currentUserName
        } else {
            "Игрок 2"
        }
        binding.statusText.text = "Ход: $playerText"
    }

    private fun setupClickListeners() {
        // Обработчики для ImageView игрового поля
        binding.button00.setOnClickListener { makeMove(0, 0, binding.button00) }
        binding.button01.setOnClickListener { makeMove(0, 1, binding.button01) }
        binding.button02.setOnClickListener { makeMove(0, 2, binding.button02) }
        binding.button10.setOnClickListener { makeMove(1, 0, binding.button10) }
        binding.button11.setOnClickListener { makeMove(1, 1, binding.button11) }
        binding.button12.setOnClickListener { makeMove(1, 2, binding.button12) }
        binding.button20.setOnClickListener { makeMove(2, 0, binding.button20) }
        binding.button21.setOnClickListener { makeMove(2, 1, binding.button21) }
        binding.button22.setOnClickListener { makeMove(2, 2, binding.button22) }

        // Кнопка новой игры
        binding.restartButton.setOnClickListener {
            initializeGame()
        }

        // Кнопка назад
        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun makeMove(row: Int, col: Int, imageView: ImageView) {
        if (!gameActive || board[row][col] != "") {
            return
        }

        // Делаем ход
        board[row][col] = currentPlayer

        // Устанавливаем картинку
        val avatarResId = if (currentPlayer == "PLAYER1") player1Avatar else player2Avatar
        imageView.setImageResource(avatarResId)
        imageView.isEnabled = false
        imageView.isClickable = false

        // Проверяем победу
        if (checkWinner()) {
            val winnerName = if (currentPlayer == "PLAYER1") currentUserName else "Игрок 2"
            binding.statusText.text = "Победил: $winnerName!"
            gameActive = false
            Toast.makeText(this, "Победил: $winnerName!", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверяем ничью
        if (isBoardFull()) {
            binding.statusText.text = "Ничья!"
            gameActive = false
            Toast.makeText(this, "Ничья!", Toast.LENGTH_SHORT).show()
            return
        }

        // Передаем ход другому игроку
        currentPlayer = if (currentPlayer == "PLAYER1") "PLAYER2" else "PLAYER1"
        updateStatusText()
    }

    private fun checkWinner(): Boolean {
        // Проверка строк
        for (i in 0 until 3) {
            if (board[i][0] == currentPlayer && board[i][1] == currentPlayer && board[i][2] == currentPlayer) {
                return true
            }
        }

        // Проверка столбцов
        for (i in 0 until 3) {
            if (board[0][i] == currentPlayer && board[1][i] == currentPlayer && board[2][i] == currentPlayer) {
                return true
            }
        }

        // Проверка диагоналей
        if (board[0][0] == currentPlayer && board[1][1] == currentPlayer && board[2][2] == currentPlayer) {
            return true
        }
        if (board[0][2] == currentPlayer && board[1][1] == currentPlayer && board[2][0] == currentPlayer) {
            return true
        }

        return false
    }

    private fun isBoardFull(): Boolean {
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[i][j] == "") {
                    return false
                }
            }
        }
        return true
    }
}