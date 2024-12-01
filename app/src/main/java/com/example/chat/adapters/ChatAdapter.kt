package com.example.chat.adapters

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chat.Const
import com.example.chat.R
import com.example.chat.models.Chat
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChatAdapter(private val context: Context, private val chatArray: ArrayList<Chat>) :
    RecyclerView.Adapter<ChatAdapter.HolderChat>() {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val MESSAGE_LEFT = 0
        private const val MESSAGE_RIGHT = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderChat {
        val layoutId = if (viewType == MESSAGE_RIGHT) R.layout.item_chat_right else R.layout.item_chat_left
        val view = LayoutInflater.from(context).inflate(layoutId, parent, false)
        return HolderChat(view)
    }

    override fun getItemCount(): Int = chatArray.size

    override fun getItemViewType(position: Int): Int {
        return if (chatArray[position].transmitterUid == firebaseAuth.uid) MESSAGE_RIGHT else MESSAGE_LEFT
    }

    override fun onBindViewHolder(holder: HolderChat, position: Int) {
        val modelChat = chatArray[position]
        val message = modelChat.message
        val typeMessage = modelChat.typeMessage
        val time = modelChat.time
        val dateFormat = Const.getDateHour(time)

        holder.tv_time_message.text = dateFormat

        if (typeMessage == Const.MESSAGE_TYPE_TEXT) {
            holder.tv_message.apply {
                visibility = View.VISIBLE
                text = message
            }
            holder.iv_message.visibility = View.GONE
            setTextMessageClickListener(holder, position, modelChat)
        } else {
            holder.tv_message.visibility = View.GONE
            holder.iv_message.visibility = View.VISIBLE
            loadImage(holder.iv_message, message)
            setImageMessageClickListener(holder, position, modelChat)
        }
    }

    private fun loadImage(imageView: ImageView, imageUrl: String) {
        runCatching {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.sub_img)
                .error(R.drawable.error_img)
                .into(imageView)
        }.onFailure {
            // Manejo del error (opcional)
        }
    }

    private fun setTextMessageClickListener(holder: HolderChat, position: Int, modelChat: Chat) {
        if (modelChat.transmitterUid == firebaseAuth.uid) {
            holder.itemView.setOnClickListener {
                showOptionsDialog(holder, position, modelChat)
            }
        }
    }

    private fun setImageMessageClickListener(holder: HolderChat, position: Int, modelChat: Chat) {
        holder.itemView.setOnClickListener {
            val options = if (modelChat.transmitterUid == firebaseAuth.uid) {
                arrayOf("Eliminar imagen", "Ver imagen", "Cancelar")
            } else {
                arrayOf("Ver imagen", "Cancelar")
            }
            showImageOptionsDialog(holder, options, position, modelChat)
        }
    }

    private fun showOptionsDialog(holder: HolderChat, position: Int, modelChat: Chat) {
        val options = arrayOf<CharSequence>("Eliminar mensaje", "Cancelar")
        AlertDialog.Builder(holder.itemView.context)
            .setTitle("¿Qué deseas realizar?")
            .setItems(options) { _, which ->
                if (which == 0) deleteMessage(position, holder, modelChat)
            }
            .show()
    }

    private fun showImageOptionsDialog(holder: HolderChat, options: Array<String>, position: Int, modelChat: Chat) {
        AlertDialog.Builder(holder.itemView.context)
            .setTitle("¿Qué deseas realizar?")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> deleteMessage(position, holder, modelChat)
                    1 -> imageVisualizer(modelChat.message)
                }
            }
            .show()
    }

    private fun imageVisualizer(image: String) {
        val dialog = Dialog(context).apply {
            setContentView(R.layout.dialog_visualizer_image)
        }

        val iv: ImageView = dialog.findViewById(R.id.PV_img)
        val btnClose: MaterialButton = dialog.findViewById(R.id.btn_close_visualizer)

        loadImage(iv, image)

        btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun deleteMessage(position: Int, holder: HolderChat, modelChat: Chat) {
        val routeChat = Const.routeChat(modelChat.receiverUid, modelChat.transmitterUid)
        FirebaseDatabase.getInstance().reference.child("Chats")
            .child(routeChat).child(chatArray[position].idMessage)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(holder.itemView.context, "Se ha eliminado el mensaje", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(holder.itemView.context, "No se ha eliminado el mensaje debido a: $e", Toast.LENGTH_SHORT).show()
            }
    }

    inner class HolderChat(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tv_message: TextView = itemView.findViewById(R.id.tv_msg)
        val iv_message: ShapeableImageView = itemView.findViewById(R.id.iv_msg)
        val tv_time_message: TextView = itemView.findViewById(R.id.tv_time_msg)
    }
}
