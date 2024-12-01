package com.example.chat.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chat.R
import com.example.chat.chat.ChatActivity
import com.example.chat.models.User

class UserAdapter(
    private val context: Context,
    private val userList: List<User>
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = userList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.apply {
            // Establecer los valores de las vistas
            username.text = user.username
            email.text = user.email
            // Si la imagen está disponible, se carga; si no, se muestra un ícono por defecto
            Glide.with(context)
                .load(user.imgProfile)
                .placeholder(R.drawable.ic_profile_img)
                .into(imgProfile)

            itemView.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("uid", user.uid) // Pasa el UID del usuario seleccionado
                }
                Toast.makeText(context, "Has seleccionado al usuario: ${user.username}", Toast.LENGTH_SHORT).show()
                context.startActivity(intent)
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val uid: TextView = itemView.findViewById(R.id.item_uid)
        val email: TextView = itemView.findViewById(R.id.item_email)
        val username: TextView = itemView.findViewById(R.id.item_username)
        val imgProfile: ImageView = itemView.findViewById(R.id.item_img)
    }
}
