package com.example.lab3android

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class Database(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_USERS = "users"

        // Колонки таблицы
        private const val COLUMN_ID = "id"
        private const val COLUMN_AVATAR_PATH = "avatar_path"
        private const val COLUMN_LOGIN = "login"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_LAST_NAME = "last_name"
        private const val COLUMN_FIRST_NAME = "first_name"
        private const val COLUMN_MIDDLE_NAME = "middle_name"
        private const val COLUMN_BIRTH_DATE = "birth_date"
        private const val COLUMN_GENDER = "gender"
        private const val COLUMN_IS_ADMIN = "is_admin"
        private const val COLUMN_THEME = "theme"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_AVATAR_PATH TEXT,
                $COLUMN_LOGIN TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_LAST_NAME TEXT NOT NULL,
                $COLUMN_FIRST_NAME TEXT NOT NULL,
                $COLUMN_MIDDLE_NAME TEXT,
                $COLUMN_BIRTH_DATE TEXT NOT NULL,
                $COLUMN_GENDER TEXT NOT NULL,
                $COLUMN_IS_ADMIN INTEGER DEFAULT 0
                $COLUMN_THEME TEXT DEFAULT 'light'
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            try {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_THEME TEXT DEFAULT 'light'") // 🔹 NEW
                Log.d("DATABASE", "Добавлена колонка theme в таблицу users")
            } catch (e: Exception) {
                Log.e("DATABASE", "Ошибка при добавлении theme: ${e.message}")
            }
        }
    }

    // МЕТОД ДЛЯ ПОДСЧЕТА ПОЛЬЗОВАТЕЛЕЙ
    fun getUsersCount(): Int {
        val db = this.readableDatabase
        val query = "SELECT COUNT(*) FROM $TABLE_USERS"
        val cursor = db.rawQuery(query, null)

        val count = if (cursor.moveToFirst()) {
            cursor.getInt(0)
        } else {
            0
        }
        cursor.close()

        Log.d("DATABASE", "Текущее количество пользователей в БД: $count")
        return count
    }

    // ПОЛУЧЕНИЕ ВСЕХ ПОЛЬЗОВАТЕЛЕЙ
    fun getAllUsers(): List<User> {
        val userList = mutableListOf<User>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS ORDER BY $COLUMN_LAST_NAME, $COLUMN_FIRST_NAME"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val user = User(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    avatarPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR_PATH)),
                    login = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOGIN)),
                    password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                    lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                    firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                    middleName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MIDDLE_NAME)),
                    birthDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTH_DATE)),
                    gender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)),
                    isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ADMIN)) == 1
                )
                userList.add(user)
            } while (cursor.moveToNext())
        }
        cursor.close()

        Log.d("DATABASE", "Загружено пользователей: ${userList.size}")
        return userList
    }

    // Регистрация нового пользователя
    fun registerUser(
        login: String,
        password: String,
        lastName: String,
        firstName: String,
        middleName: String?,
        birthDate: String,
        gender: String,
        avatarPath: String? = null
    ): Boolean {
        val db = this.writableDatabase

        // ПРОВЕРЯЕМ, ПЕРВЫЙ ЛИ ЭТО ПОЛЬЗОВАТЕЛЬ
        val isFirstUser = getUsersCount() == 0

        val values = ContentValues().apply {
            put(COLUMN_LOGIN, login)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_LAST_NAME, lastName)
            put(COLUMN_FIRST_NAME, firstName)
            put(COLUMN_MIDDLE_NAME, middleName)
            put(COLUMN_BIRTH_DATE, birthDate)
            put(COLUMN_GENDER, gender)
            put(COLUMN_AVATAR_PATH, avatarPath)

            // ЕСЛИ ПЕРВЫЙ ПОЛЬЗОВАТЕЛЬ - ДЕЛАЕМ АДМИНИСТРАТОРОМ
            put(COLUMN_IS_ADMIN, if (isFirstUser) 1 else 0)
        }

        return try {
            val result = db.insert(TABLE_USERS, null, values)

            // ЛОГИРУЕМ ДЛЯ ПРОВЕРКИ
            if (result != -1L) {
                if (isFirstUser) {
                    Log.d("DATABASE", "Первый пользователь $login зарегистрирован как АДМИНИСТРАТОР")
                } else {
                    Log.d("DATABASE", "Пользователь $login зарегистрирован как обычный пользователь")
                }
            }

            result != -1L
        } catch (e: Exception) {
            Log.e("DATABASE", "Ошибка регистрации пользователя: ${e.message}")
            false
        }
    }

    // Проверка существования логина
    fun isLoginExists(login: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_LOGIN = ?"
        val cursor = db.rawQuery(query, arrayOf(login))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Добавьте этот метод в класс Database

    fun updateUser(
        userId: Long,
        login: String,
        password: String,
        lastName: String,
        firstName: String,
        middleName: String?,
        birthDate: String,
        gender: String,
        avatarPath: String? = null,
        isAdmin: Boolean = false
    ): Boolean {
        val db = this.writableDatabase

        val values = ContentValues().apply {
            put(COLUMN_LOGIN, login)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_LAST_NAME, lastName)
            put(COLUMN_FIRST_NAME, firstName)
            put(COLUMN_MIDDLE_NAME, middleName)
            put(COLUMN_BIRTH_DATE, birthDate)
            put(COLUMN_GENDER, gender)
            put(COLUMN_AVATAR_PATH, avatarPath)
            put(COLUMN_IS_ADMIN, if (isAdmin) 1 else 0)
        }

        return try {
            val result = db.update(TABLE_USERS, values, "$COLUMN_ID = ?", arrayOf(userId.toString()))
            Log.d("DATABASE", "Пользователь $userId обновлен. Результат: $result")
            result > 0
        } catch (e: Exception) {
            Log.e("DATABASE", "Ошибка обновления пользователя: ${e.message}")
            false
        }
    }

    // Метод для получения пользователя по ID
    fun getUserById(userId: Long): User? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                avatarPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR_PATH)),
                login = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOGIN)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                middleName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MIDDLE_NAME)),
                birthDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTH_DATE)),
                gender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)),
                isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ADMIN)) == 1
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    // В класс Database добавляем метод
    fun getUserByLogin(login: String): User? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_LOGIN = ?"
        val cursor = db.rawQuery(query, arrayOf(login))

        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                avatarPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR_PATH)),
                login = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOGIN)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                middleName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MIDDLE_NAME)),
                birthDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTH_DATE)),
                gender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)),
                isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ADMIN)) == 1
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }


    // Получение пользователя по логину и паролю
    fun getUser(login: String, password: String): User? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_LOGIN = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(login, password))

        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                avatarPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AVATAR_PATH)),
                login = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOGIN)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)),
                lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                middleName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MIDDLE_NAME)),
                birthDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTH_DATE)),
                gender = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)),
                isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ADMIN)) == 1
            )
            cursor.close()
            user
        } else {
            cursor.close()
            null
        }
    }

    // Получить тему пользователя
    fun getUserTheme(login: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT theme FROM users WHERE login = ?", arrayOf(login))
        var theme = "light"
        if (cursor.moveToFirst()) {
            theme = cursor.getString(0)
        }
        cursor.close()
        return theme
    }

    // Сохранить тему пользователя
    fun setUserTheme(login: String, theme: String) {
        val db = writableDatabase
        val values = ContentValues()
        values.put("theme", theme)
        db.update("users", values, "login = ?", arrayOf(login))
    }


    // Удалить всех пользователей
    fun deleteAllUsers(): Boolean {
        val db = this.writableDatabase
        return try {
            db.delete(TABLE_USERS, null, null) > 0
        } catch (e: Exception) {
            false
        }
    }

    fun getUserAvatar(userId: Long): ByteArray? {
        val db = this.readableDatabase
        val query = "SELECT $COLUMN_AVATAR_PATH FROM $TABLE_USERS WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        return if (cursor.moveToFirst()) {
            val avatarBlob = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_AVATAR_PATH))
            cursor.close()
            if (avatarBlob != null && avatarBlob.isNotEmpty()) avatarBlob else null
        } else {
            cursor.close()
            null
        }
    }
}