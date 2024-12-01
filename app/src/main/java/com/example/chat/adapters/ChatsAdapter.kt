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
import com.google.firebase.database.*

class ChatsAdapter(
    private val context: Context,
    private val chatArrayList: ArrayList<Chats>
) : RecyclerView.Adapter<ChatsAdapter.HolderChats>() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val miUid = firebaseAuth.uid!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChats {
        val binding = ItemChatsBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderChats(binding)
    }

    override fun getItemCount(): Int = chatArrayList.size

    override fun onBindViewHolder(holder: HolderChats, position: Int) {
        val chatsModel = chatArrayList[position]
        loadLastMessage(chatsModel, holder)

        holder.itemView.setOnClickListener {
            val receiverUid = chatsModel.receiverUid
            receiverUid?.let {
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("uid", it)
                }
                context.startActivity(intent)
            }
        }
    }

    private fun loadLastMessage(chatsModel: Chats, holder: HolderChats) {
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatsModel.keyChat).limitToLast(1)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { ds ->
                        val transmitterUid = ds.child("transmitterUid").value.toString()
                        val message = ds.child("message").value.toString()
                        val messageType = ds.child("typeMessage").value.toString()
                        val time = ds.child("time").value as Long

                        chatsModel.apply {
                            this.transmittorUid = transmitterUid
                            this.message = message
                            this.typeMessage = messageType
                        }

                        holder.tvDate.text = Const.getDateHour(time)
                        holder.tvLastMsg.text = if (messageType == Const.MESSAGE_TYPE_TEXT) {
                            message
                        } else {
                            "Se ha enviado una imagen"
                        }

                        loadUserInfo(chatsModel, holder)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error if necessary
                }
            })
    }

    private fun loadUserInfo(chatsModel: Chats, holder: HolderChats) {
        val uidReceiver = if (chatsModel.transmittorUid == miUid) {
            chatsModel.receiverUid
        } else {
            chatsModel.transmittorUid
        }

        uidReceiver?.let {
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val name = snapshot.child("names").value.toString()
                        val image = snapshot.child("image").value.toString()

                        chatsModel.names = name
                        chatsModel.image = image

                        holder.tvNames.text = name

                        Glide.with(context)
                            .load(image)
                            .placeholder(R.drawable.ic_profile_img)
                            .into(holder.IvProfile)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error if necessary
                    }
                })
        }
    }

    inner class HolderChats(private val binding: ItemChatsBinding) : RecyclerView.ViewHolder(binding.root) {
        val IvProfile = binding.IvProfile
        val tvNames = binding.tvNames
        val tvLastMsg = binding.tvLastMessage
        val tvDate = binding.tvDate
    }
}
