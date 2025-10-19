package com.example.lab3android

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var users: List<User> = emptyList()
    private var listener: OnUserClickListener? = null

    interface OnUserClickListener {
        fun onUserClick(user: User)
    }

    fun setOnUserClickListener(listener: OnUserClickListener) {
        this.listener = listener
    }

    fun setUsers(users: List<User>) {
        this.users = users
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val avatarImageView: ImageView = itemView.findViewById(R.id.avatarImageView)
        private val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        private val loginTextView: TextView = itemView.findViewById(R.id.loginTextView)
        private val adminStatusTextView: TextView = itemView.findViewById(R.id.adminStatusTextView)

        fun bind(user: User) {
            // Формируем ФИО
            val fullName = "${user.lastName} ${user.firstName} ${user.middleName ?: ""}".trim()
            userNameTextView.text = fullName

            // Логин
            loginTextView.text = user.login

            // Статус администратора
            if (user.isAdmin) {
                adminStatusTextView.visibility = View.VISIBLE
                adminStatusTextView.text = "Администратор"
            } else {
                adminStatusTextView.visibility = View.GONE
            }

            // ЗАГРУЗКА АВАТАРА ИЗ РЕСУРСОВ
            loadUserAvatar(user)

            // Обработка клика на элемент
            itemView.setOnClickListener {
                listener?.onUserClick(user)
            }
        }

        private fun loadUserAvatar(user: User) {
            // Если аватар сохранен как ресурс (avatar_res_2131165310)
            if (user.avatarPath?.startsWith("avatar_res_") == true) {
                try {
                    // Извлекаем ID ресурса из строки
                    val resIdString = user.avatarPath.removePrefix("avatar_res_")
                    val resId = resIdString.toInt()

                    // Пытаемся установить картинку из ресурсов
                    avatarImageView.setImageResource(resId)

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

        private fun setDefaultAvatar() {
            try {
                // Пытаемся установить аватар по умолчанию
                avatarImageView.setImageResource(R.drawable.avatar_default)
            } catch (e: Resources.NotFoundException) {
                // Если даже аватар по умолчанию не найден - используем системную иконку
                avatarImageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        }
    }
}