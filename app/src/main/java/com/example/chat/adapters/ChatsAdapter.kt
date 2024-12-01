package com.example.chat.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chat.Const
import com.example.chat.R
import com.example.chat.chat.ChatActivity
import com.example.chat.databinding.ItemChatsBinding
import com.example.chat.models.Chats
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.*

class ChatsAdapter(
    private val context: Context,
    private val chatArrayList: ArrayList<Chats>
) : RecyclerView.Adapter<ChatsAdapter.HolderChats>() {

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
            if (receiverUid != null) {
                val intent = Intent(context, ChatActivity::class.java).apply {
                    putExtra("uid", receiverUid)
                }
                context.startActivity(intent)
            }
        }
    }

    private fun loadLastMessage(chatsModel: Chats, holder: HolderChats) {
        val ref = FirebaseDatabase.getInstance().getReference("Chats").child(chatsModel.keyChat)
        ref.limitToLast(1).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { ds ->
                    chatsModel.transmittorUid = ds.child("transmitterUid").value.toString()
                    chatsModel.message = ds.child("message").value.toString()
                    chatsModel.typeMessage = ds.child("typeMessage").value.toString()
                    val time = ds.child("time").value as Long

                    holder.tvDate.text = Const.getDateHour(time)
                    holder.tvLastMsg.text = if (chatsModel.typeMessage == Const.MESSAGE_TYPE_TEXT) {
                        chatsModel.message
                    } else {
                        "Se ha enviado una imagen"
                    }

                    loadUserInfo(chatsModel, holder)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                FirebaseCrashlytics.getInstance().log("Error loading last message: ${error.message}")
                FirebaseCrashlytics.getInstance().recordException(error.toException())
            }
        })
    }

    private fun loadUserInfo(chatsModel: Chats, holder: HolderChats) {
        val uidReceiver = if (chatsModel.transmittorUid == FirebaseAuth.getInstance().uid) {
            chatsModel.receiverUid
        } else {
            chatsModel.transmittorUid
        }

        val ref = FirebaseDatabase.getInstance().getReference("Users").child(uidReceiver ?: return)
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("names").value?.toString() ?: "Usuario desconocido"
                val image = snapshot.child("image").value?.toString() ?: ""

                holder.tvNames.text = name
                Glide.with(context)
                    .load(image)
                    .placeholder(R.drawable.ic_profile_img)
                    .into(holder.ivProfile)
            }

            override fun onCancelled(error: DatabaseError) {
                FirebaseCrashlytics.getInstance().log("Error loading user info: ${error.message}")
                FirebaseCrashlytics.getInstance().recordException(error.toException())
            }
        })
    }

    inner class HolderChats(binding: ItemChatsBinding) : RecyclerView.ViewHolder(binding.root) {
        val ivProfile = binding.IvProfile
        val tvNames = binding.tvNames
        val tvLastMsg = binding.tvLastMessage
        val tvDate = binding.tvDate
    }
}
