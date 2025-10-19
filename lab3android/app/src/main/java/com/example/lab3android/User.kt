package com.example.lab3android

data class User(
    val id: Long = 0,
    val avatarPath: String? = null,  // путь к картинке аватара
    val login: String,               // логин
    val password: String,            // пароль
    val lastName: String,            // фамилия
    val firstName: String,           // имя
    val middleName: String? = null,  // отчество (может быть пустым)
    val birthDate: String,           // дата рождения
    val gender: String,              // пол
    val isAdmin: Boolean = false     // админ или нет
)
