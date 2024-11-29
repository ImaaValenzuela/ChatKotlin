package com.example.chat.adapters

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
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

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.HolderChat>{

    private val context : Context
    private val chatArray : ArrayList<Chat>
    private val firebaseAuth : FirebaseAuth
    private var routeChat = ""

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

    private fun imageVisualizer(image: String) {
        val iv: ImageView
        val btnClose: MaterialButton

        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_visualizer_image)

        iv = dialog.findViewById(R.id.PV_img)  // Cambiado de PhotoView a ImageView
        btnClose = dialog.findViewById(R.id.btn_close_visualizer)

        try {
            Glide.with(context)
                .load(image)
                .placeholder(R.drawable.sub_img)
                .into(iv)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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

            holder.tv_message.text = message

            if(modelChat.transmitterUid == firebaseAuth.uid){
                holder.itemView.setOnClickListener {
                    val options = arrayOf<CharSequence>("Eliminar mensaje", "Cancelar")
                    val builder : AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Que desea realizar?")
                    builder.setItems(options, DialogInterface.OnClickListener{dialog, which ->
                        if(which == 0){
                            deleteMessage(position, holder, modelChat)
                        }
                    })

                    builder.show()
                }
            }


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

            if(modelChat.transmitterUid == firebaseAuth.uid){
                holder.itemView.setOnClickListener {
                    val options = arrayOf<CharSequence>("Eliminar imagen", "Ver imagen", "Cancelar")
                    val builder : AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Que desea realizar?")
                    builder.setItems(options, DialogInterface.OnClickListener{dialog, which ->
                        if(which == 0){
                            deleteMessage(position, holder, modelChat)
                        } else if (which == 1){
                            imageVisualizer(modelChat.message)
                        }
                    })

                    builder.show()
                }
            } else if(modelChat.transmitterUid != firebaseAuth.uid){
                holder.itemView.setOnClickListener {
                    val options = arrayOf<CharSequence>("Ver imagen", "Cancelar")
                    val builder : AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
                    builder.setTitle("Que desea realizar?")
                    builder.setItems(options, DialogInterface.OnClickListener{dialog, which ->
                        if(which == 0){
                            imageVisualizer(modelChat.message)
                        }
                    })

                    builder.show()
                }
            }
        }

    }

    private fun deleteMessage(position: Int, holder: HolderChat, modelChat: Chat) {
        routeChat = Const.routeChat(modelChat.receiverUid, modelChat.transmitterUid)

        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref.child(routeChat).child(chatArray.get(position).idMessage)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(
                    holder.itemView.context,
                    "Se ha eliminado el mensaje",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    holder.itemView.context,
                    "No se ha eliminado el mensaje debido a ${e}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    inner class HolderChat (itemView: View) : RecyclerView.ViewHolder(itemView){
        var tv_message : TextView = itemView.findViewById(R.id.tv_msg)
        var iv_message : ShapeableImageView = itemView.findViewById(R.id.iv_msg)
        var tv_time_message : TextView = itemView.findViewById(R.id.tv_time_msg
        )
    }

}