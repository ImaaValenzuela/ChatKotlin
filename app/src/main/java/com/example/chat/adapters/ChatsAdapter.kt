package com.example.chat.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chat.Const
import com.example.chat.R
import com.example.chat.chat.ChatActivity
import com.example.chat.databinding.ItemChatsBinding
import com.example.chat.models.Chats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatsAdapter : RecyclerView.Adapter<ChatsAdapter.HolderChats>{
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsAdapter.HolderChats {
        binding = ItemChatsBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderChats(binding.root)
    }

    override fun getItemCount(): Int {
        return chatArrayList.size
    }

    override fun onBindViewHolder(holder: HolderChats, position: Int) {
        val chatsModel = chatArrayList[position]

        loadLastMessage(chatsModel, holder)

        holder.itemView.setOnClickListener {
            val receiverUid = chatsModel.receiverUid
            if( receiverUid!= null){
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("uid", receiverUid)
                context.startActivity(intent)
            }
        }
    }

    private fun loadLastMessage(chatsModel: Chats, holder: HolderChats) {
        val chatKey = chatsModel.keyChat

        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatKey).limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (ds in snapshot.children){
                        val transmitterUid = "${ds.child("transmitterUid").value}"
                        val messageId = "${ds.child("idMessage").value}"
                        val message = "${ds.child("message").value}"
                        val receiverUid = "${ds.child("receiverUid").value}"
                        val time = ds.child("time").value as Long
                        val messageType = "${ds.child("typeMessage").value}"

                        val dateFormat = Const.getDateHour(time)

                        chatsModel.transmittorUid = transmitterUid
                        chatsModel.idMessage = messageId
                        chatsModel.message = message
                        chatsModel.receiverUid = receiverUid
                        chatsModel.typeMessage = messageType

                        holder.tvDate.text = dateFormat

                        if(messageType == Const.MESSAGE_TYPE_TEXT){
                            holder.tvLastMsg.text = message
                        } else {
                            holder.tvLastMsg.text = "Se ha enviado una imagen"
                        }

                        loadUserInfo(chatsModel, holder)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun loadUserInfo(chatsModel: Chats, holder: HolderChats) {
        val transmitterUid = chatsModel.transmittorUid
        val receiverUid = chatsModel.uidReceiver

        var uidReceiver = ""

        if (transmitterUid == miUid){
            uidReceiver = receiverUid
        } else {
            uidReceiver = transmitterUid
        }

        chatsModel.uidReceiver = uidReceiver

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uidReceiver)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("names")}"
                    val image = "${snapshot.child("image")}"

                    chatsModel.names = name
                    chatsModel.image = image

                    holder.tvNames.text = name

                    try {
                        Glide.with(context)
                            .load(image)
                            .placeholder(R.drawable.ic_profile_img)
                            .into(holder.IvProfile)
                    }catch (e : Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    inner class HolderChats (itemView : View) : RecyclerView.ViewHolder(itemView){
        var IvProfile = binding.IvProfile
        var tvNames = binding.tvNames
        var tvLastMsg = binding.tvLastMessage
        var tvDate = binding.tvDate
    }


}