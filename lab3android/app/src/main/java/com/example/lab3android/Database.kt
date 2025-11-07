package com.example.lab3android

import android.content.ContentValues    // Класс для хранения набора значений (ключ-значение) для вставки/обновления в БД
import android.content.Context          // Класс предоставляет доступ к ресурсам приложения, БД, файлам
import android.database.sqlite.SQLiteDatabase    // Класс для работы с SQLite базой данных (запросы, транзакции)
import android.database.sqlite.SQLiteOpenHelper  // Базовый класс для управления созданием и версиями БД
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
                $COLUMN_IS_ADMIN INTEGER DEFAULT 0,
                $COLUMN_THEME TEXT DEFAULT 'light'
            );
        """.trimIndent()
        db.execSQL(createTable)
    }

    // Метод вызывается при обновлении версии базы данных
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {   // Проверяем, если старая версия меньше 2 (добавлена колонка theme в версии 2)
            try {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_THEME TEXT DEFAULT 'light'") // 🔹 NEW
                Log.d("DATABASE", "Добавлена колонка theme в таблицу users")
            } catch (e: Exception) {
                Log.e("DATABASE", "Ошибка при добавлении theme: ${e.message}")
            }
        }
    }

    // Метод для подсчета пользователей
    fun getUsersCount(): Int {
        val db = this.readableDatabase   // Получаем объект базы данных для чтения
        val query = "SELECT COUNT(*) FROM $TABLE_USERS"
        val cursor = db.rawQuery(query, null)    // Выполнение запроса и получение Cursor для доступа к результатам

        // Получение количества пользователей из Cursor
        val count = if (cursor.moveToFirst()) { // Перемещаем Cursor на первую запись
            cursor.getInt(0)        // Получаем значение из первой колонки (COUNT(*))
        } else {
            0
        }
        cursor.close()                           // Всегда закрываем Cursor после использования для освобождения ресурсов

        Log.d("DATABASE", "Текущее количество пользователей в БД: $count")
        return count
    }

    // Возвращ список всех пользователей
    fun getAllUsers(): List<User> {
        val userList = mutableListOf<User>()     // Создаем изменяемый список для хранения пользователей
        val db = this.readableDatabase           // Получаем объект базы данных для чтения
        val query = "SELECT * FROM $TABLE_USERS ORDER BY $COLUMN_LAST_NAME, $COLUMN_FIRST_NAME"     // SQL запрос для получения всех пользователей с сортировкой по фамилии и имени
        val cursor = db.rawQuery(query, null)       // Выполнение запроса

        if (cursor.moveToFirst()) {
            do {
                val user = User(        // Создаем объект User из данных текущей записи Cursor
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
                userList.add(user)       // Добавляем пользователя в список
            } while (cursor.moveToNext())    // Переходим к следующей записи, пока они есть
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

        // Проверяем, первый ли это пользователь
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

            // Если это первый пользователь - делаем админом
            put(COLUMN_IS_ADMIN, if (isFirstUser) 1 else 0)
        }

        return try {
            // Вставляем данные в таблицу и получаем ID новой записи
            val result = db.insert(TABLE_USERS, null, values)

            // Проверка
            if (result != -1L) {
                if (isFirstUser) {
                    Log.d("DATABASE", "Первый пользователь $login зарегистрирован как АДМИНИСТРАТОР")
                } else {
                    Log.d("DATABASE", "Пользователь $login зарегистрирован как обычный пользователь")
                }
            }

            // Возвращаем true если вставка успешна (result != -1), иначе false
            result != -1L
        } catch (e: Exception) {
            Log.e("DATABASE", "Ошибка регистрации пользователя: ${e.message}")
            false
        }
    }

    // Проверка существования логина
    fun isLoginExists(login: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_LOGIN = ?"     // SQL запрос для поиска пользователя с указанным логином
        val cursor = db.rawQuery(query, arrayOf(login))     // Задали параметр login вместо ?
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

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
        val db = this.writableDatabase      // Получаем объект базы данных для записи

        val values = ContentValues().apply {        // Создаем ContentValues с новыми данными пользователя
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
            // Обновляем запись пользователя по ID
            val result = db.update(TABLE_USERS, values, "$COLUMN_ID = ?", arrayOf(userId.toString()))
            Log.d("DATABASE", "Пользователь $userId обновлен. Результат: $result")
            // Возвращаем true если обновлена хотя бы одна запись
            result > 0
        } catch (e: Exception) {
            Log.e("DATABASE", "Ошибка обновления пользователя: ${e.message}")
            false
        }
    }

    // Метод для получения пользователя по ID
    fun getUserById(userId: Long): User? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_ID = ?"       // SQL запрос для поиска пользователя по ID
        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        // Проверяем, найден ли пользователь
        return if (cursor.moveToFirst()) {
            val user = User(        // Создаем объект User из данных Cursor
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
            user                    // Возвращаем найденного пользователя
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
        // SQL запрос для поиска пользователя по логину И паролю
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_LOGIN = ? AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(login, password))

        // Проверяем, найден ли пользователь
        return if (cursor.moveToFirst()) {
            val user = User(            // Создаем объект User из данных Cursor
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
        // SQL запрос для получения темы пользователя
        val cursor = db.rawQuery("SELECT theme FROM users WHERE login = ?", arrayOf(login))
        var theme = "light"
        if (cursor.moveToFirst()) {      // Проверяем, есть ли результат
            theme = cursor.getString(0)      // Получаем тему из первой колонки результата
        }
        cursor.close()
        return theme
    }

    // Сохранить тему пользователя
    fun setUserTheme(login: String, theme: String) {
        val db = writableDatabase
        val values = ContentValues()         // Создаем ContentValues с новой темой
        values.put("theme", theme)           // Добавляем тему

        // Обновляем запись пользователя
        db.update("users", values, "login = ?", arrayOf(login))
    }
}