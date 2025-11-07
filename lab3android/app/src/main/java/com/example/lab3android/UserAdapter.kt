package com.example.lab3android

import android.content.res.Resources        // Класс для работы с ресурсами приложения
import android.view.LayoutInflater          // Класс для создания View из XML layout
import android.view.View
import android.view.ViewGroup               // Контейнер для View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView     // Базовый класс адаптера для RecyclerView
import com.example.lab3android.databinding.ItemUserBinding

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {            // Адаптер для отображения списка пользователей в RecyclerView

    private var users: List<User> = emptyList()          // Список пользователей для отображения (изначально пустой)
    private var listener: OnUserClickListener? = null    // Слушатель кликов на элементах списка

    interface OnUserClickListener {
        fun onUserClick(user: User)                      // Метод вызывается при клике на пользователя
    }

    fun setOnUserClickListener(listener: OnUserClickListener) {     // Метод для установки слушателя кликов
        this.listener = listener
    }

    fun setUsers(users: List<User>) {       // Метод для обновления списка пользователей
        this.users = users                  // Устанавливаем новый список
        notifyDataSetChanged()              // Уведомляем адаптер об изменении данных
    }

    // Создает новый ViewHolder когда RecyclerView нуждается в нем
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
       //val view = LayoutInflater.from(parent.context)      // Создаем View из XML layout файла item_user.xml
       //    .inflate(R.layout.item_user, parent, false)
       // return UserViewHolder(view)
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder (binding)
    }

    // Связывает данные с ViewHolder на определенной позиции
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])     // Передаем пользователя в ViewHolder для отображения
    }

    override fun getItemCount(): Int = users.size       // Возвращает общее количество элементов в списке

    // Внутренний класс ViewHolder для хранения ссылок на View элементы
    inner class UserViewHolder(private val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root) {
        //private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)         // Объявление переменных для элементов item_user.xml
        //private val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        //private val loginTextView: TextView = itemView.findViewById(R.id.loginTextView)
        //private val adminStatusTextView: TextView = itemView.findViewById(R.id.adminStatusTextView)

        // Метод для привязки данных пользователя к View элементам
        fun bind(user: User) {
            // Формируем ФИО
            val fullName = "${user.lastName} ${user.firstName} ${user.middleName ?: ""}".trim()
            binding.userNameTextView.text = fullName

            // Логин
            binding.loginTextView.text = user.login

            // Статус администратора
            if (user.isAdmin) {
                binding.adminStatusTextView.visibility = View.VISIBLE
                binding.adminStatusTextView.text = "Администратор"
            } else {
                binding.adminStatusTextView.visibility = View.GONE
            }

            // Загрузка аватара из ресурсов
            loadUserAvatar(user)

            // Обработка клика на элемент
            binding.root.setOnClickListener {
                listener?.onUserClick(user)
            }
        }

        // Метод для загрузки аватара пользователя
        private fun loadUserAvatar(user: User) {
            // Если аватар сохранен как ресурс (avatar_res_2131165310)
            if (user.avatarPath?.startsWith("avatar_res_") == true) {
                try {
                    // Извлекаем ID ресурса из строки
                    val resIdString = user.avatarPath.removePrefix("avatar_res_")
                    val resId = resIdString.toInt()

                    // Пытаемся установить картинку из ресурсов
                    binding.avatarImageView.setImageResource(resId)

                } catch (e: NumberFormatException) {
                    // Если не удалось преобразовать в число - ставим аватар по умолчанию
                    setDefaultAvatar()
                } catch (e: Resources.NotFoundException) {
                    // Если ресурс не найден - ставим аватар по умолчанию
                    setDefaultAvatar()
                }
            } else {
                // Если аватар не установлен или в другом формате - используем стандартный
                setDefaultAvatar()
            }
        }

        // Метод для установки аватара по умолчанию
        private fun setDefaultAvatar() {
            try {
                // Пытаемся установить аватар по умолчанию
                binding.avatarImageView.setImageResource(R.drawable.avatar_default)
            } catch (e: Resources.NotFoundException) {
                // Если даже аватар по умолчанию не найден - используем системную иконку
                binding.avatarImageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }
}