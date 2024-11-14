package com.example.chat.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chat.Const
import com.example.chat.R
import com.example.chat.models.Chat
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.HolderChat>{

    private val context : Context
    private val chatArray : ArrayList<Chat>
    private val firebaseAuth : FirebaseAuth

    companion object{
        private const val MESSAGE_LEFT = 0
        private const val MESSAGE_RIGHT = 1
    }

    constructor(context: Context, chatArray: ArrayList<Chat>) {
        this.context = context
        this.chatArray = chatArray
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChat {
        return if(viewType == MESSAGE_RIGHT){
            val view = LayoutInflater.from(context).inflate(R.layout.item_chat_right, parent, false)
            HolderChat(view)
        } else{
            val view = LayoutInflater.from(context).inflate(R.layout.item_chat_left, parent, false)
            HolderChat(view)
        }
    }

    override fun getItemCount(): Int {
        return  chatArray.size
    }

    override fun getItemViewType(position: Int): Int {
        if(chatArray[position].transmitterUid == firebaseAuth.uid){
            return MESSAGE_RIGHT
        } else{
            return MESSAGE_LEFT
        }
    }

    override fun onBindViewHolder(holder: HolderChat, position: Int) {
        val modelChat = chatArray[position]

        val message = modelChat.message
        val typeMessage = modelChat.typeMessage
        val time = modelChat.time

        val date_format = Const.getDateHour(time)
        holder.tv_time_message.text = date_format

        if(typeMessage == Const.MESSAGE_TYPE_TEXT){
            holder.tv_message.visibility = View.VISIBLE
            holder.iv_message.visibility = View.GONE
        } else {
            holder.tv_message.visibility = View.GONE
            holder.iv_message.visibility = View.VISIBLE

            try {
                Glide.with(context)
                    .load(message)
                    .placeholder(R.drawable.sub_img)
                    .error(R.drawable.error_img)
                    .into(holder.iv_message)
            } catch (e : Exception){

            }
        }
    }

    inner class HolderChat (itemView: View) : RecyclerView.ViewHolder(itemView){
        var tv_message : TextView = itemView.findViewById(R.id.tv_msg)
        var iv_message : ShapeableImageView = itemView.findViewById(R.id.iv_msg)
        var tv_time_message : TextView = itemView.findViewById(R.id.tv_time_msg
        )
    }

}