package com.example.chat.chat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.chat.Const
import com.example.chat.R
import com.example.chat.adapters.ChatAdapter
import com.example.chat.databinding.ActivityChatBinding
import com.example.chat.models.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ChatActivity : AppCompatActivity() {


    private lateinit var binding : ActivityChatBinding
    private var uid = "" // uid del receptor

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private var miUid = "" // uid del emisor

    private var chatRoute = ""
    private var imageUri : Uri?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        uid = intent.getStringExtra("uid")!!
        miUid = firebaseAuth.uid!!

        FirebaseCrashlytics.getInstance().setUserId(uid) // Configurar ID de usuario y modelo de dispositivo en Crashlytics
        FirebaseCrashlytics.getInstance().setCustomKey("Device_Model", Build.MODEL)

        chatRoute = Const.routeChat(uid, miUid)

        binding.adjFAB.setOnClickListener{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                galleryImage()
            } else {
                reqStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        binding.IbBack.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        binding.sendFAB.setOnClickListener{
            validateMessage()
        }

        loadInfo()
        loadMessages()
    }

    private fun loadMessages() {
        val messageArrayList = ArrayList<Chat>()
        val ref = FirebaseDatabase.getInstance().getReference("Chats")
        ref.child(chatRoute)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageArrayList.clear()
                    for (ds : DataSnapshot in snapshot.children){
                        try {
                            val chat = ds.getValue(Chat::class.java)
                            messageArrayList.add(chat!!)
                        } catch (e : Exception){

                        }
                    }

                    val adapterChat = ChatAdapter(this@ChatActivity, messageArrayList)
                    binding.chatsRV.adapter = adapterChat
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun validateMessage() {
        val message = binding.etMsgChat.text.toString().trim()
        val time = Const.getTimeD()

        if(message.isEmpty()){
            Toast.makeText(
                this,
                "Ingrese un mensaje",
                Toast.LENGTH_SHORT
            ).show()
        } else{
            sendMessage(Const.MESSAGE_TYPE_TEXT, message, time)
        }
    }

    private fun loadInfo(){
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("names").value}"
                    val image = "${snapshot.child("image").value}"

                    binding.txtUserName.text = name

                    try {
                        Glide.with(this@ChatActivity)
                            .load(image)
                            .placeholder(R.drawable.user_profile)
                            .into(binding.toolbarIV)
                    } catch ( e : Exception){
                        FirebaseCrashlytics.getInstance().recordException(e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    FirebaseCrashlytics.getInstance().recordException(error.toException())
                }
            })
    }

    private fun galleryImage(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultGalleryARL.launch(intent)
    }

    private val resultGalleryARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == Activity.RESULT_OK) {
                try {
                    val data = res.data
                    imageUri = data!!.data
                    subStorageImage()
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Toast.makeText(
                        this,
                        "Ocurrió un error al seleccionar la imagen",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private val reqStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            if(isGranted){
                galleryImage()
            } else{
                Toast.makeText(
                    this,
                    "El permiso de almacenamiento no fue concedido",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun subStorageImage(){
        progressDialog.setMessage("Subiendo Imagen")
        progressDialog.show()

        val time = Const.getTimeD()
        val routeNameImage = "chatImages/$time"
        val storageRef = FirebaseStorage.getInstance().getReference(routeNameImage)
        storageRef.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val urlImage = uriTask.result.toString()
                if(uriTask.isSuccessful){
                    sendMessage(Const.MESSAGE_TYPE_IMAGE, urlImage, time)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "No se pudo enviar la imagen debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }
    }

    private fun sendMessage(messageTypeText: String, message: String, time: Long) {
        progressDialog.setMessage("Enviando Mensaje")
        progressDialog.show()

        val refChat = FirebaseDatabase.getInstance().getReference("Chats")
        val keyId = "${refChat.push().key}"
        val hashMap = HashMap<String, Any>()

        hashMap["idMessage"] = "${keyId}"
        hashMap["typeMessage"] = "${messageTypeText}"
        hashMap["message"] = "${message}"
        hashMap["transmitterUid"] = "${miUid}"
        hashMap["receiverUid"] = "$uid"
        hashMap["time"] = time

        refChat.child(chatRoute)
            .child(keyId)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                binding.etMsgChat.setText("")
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "No se pudo enviar el mensaje debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}