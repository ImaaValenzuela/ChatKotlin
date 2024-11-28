package com.example.chat.adapters

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.chat.databinding.ItemChatsBinding
import com.example.chat.models.Chats
import com.google.firebase.auth.FirebaseAuth

class ChatsAdapter {
    private var context : Context
    private var chatArrayList : ArrayList<Chats>
    private lateinit var binding : ItemChatsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private var miUid = ""

    constructor(context: Context, chatArrayList: ArrayList<Chats>) {
        this.context = context
        this.chatArrayList = chatArrayList
        firebaseAuth = FirebaseAuth.getInstance()
        miUid = firebaseAuth.uid!!
    }

    inner class HolderChats (itemView : View) : RecyclerView.ViewHolder(itemView){
        var IvProfile = binding.IvProfile
        var tvNames = binding.tvNames
        var tvLastMsg = binding.tvLastMessage
        var tvDate = binding.tvDate
    }
}