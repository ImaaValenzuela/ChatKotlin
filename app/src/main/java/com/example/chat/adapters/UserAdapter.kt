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

class UserAdapter (
    context : Context,
    userList : List<User>) : RecyclerView.Adapter<UserAdapter.ViewHolder?>(){

        private val context : Context
        private val userList : List<User>

        init {
            this.context = context
            this.userList = userList
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view : View = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user : User = userList[position]
        holder.uid.text = user.uid
        holder.email.text = user.email
        holder.username.text = user.username
        Glide.with(context).load(user.imgProfile).placeholder(R.drawable.ic_profile_img).into(holder.imgProfile)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("uid", holder.uid.text)
            Toast.makeText(context, "Has seleccionado al usuario: ${holder.username.text}" , Toast.LENGTH_SHORT).show()
            context.startActivity(intent)
        }
    }

        class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
            var uid : TextView
            var email : TextView
            var username : TextView
            var imgProfile : ImageView

            init {
                uid = itemView.findViewById(R.id.item_uid)
                email = itemView.findViewById(R.id.item_email)
                username = itemView.findViewById(R.id.item_username)
                imgProfile = itemView.findViewById(R.id.item_img)
            }
        }

}