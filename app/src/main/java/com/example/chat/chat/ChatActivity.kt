package com.example.chat.chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.chat.Const
import com.example.chat.R
import com.example.chat.adapters.ChatAdapter
import com.example.chat.databinding.ActivityChatBinding
import com.example.chat.models.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private var uid = "" // uid del receptor
    private var miUid = "" // uid del emisor
    private var chatRoute = ""
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityChatBinding.inflate(layoutInflater)
            setContentView(binding.root)

            firebaseAuth = FirebaseAuth.getInstance()

            uid = intent.getStringExtra("uid") ?: ""
            if (uid.isEmpty()) {
                showError("UID no válido, cerrando la aplicación.")
                finish()
                return
            }

            miUid = firebaseAuth.uid.orEmpty()
            chatRoute = Const.routeChat(uid, miUid)

            FirebaseCrashlytics.getInstance().apply {
                setUserId(uid)
                setCustomKey("Device_Model", Build.MODEL)
            }

            setupListeners()
            loadInfo()
            loadMessages()
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            showError("Error inesperado: ${e.message}")
            finish()
        }
    }

    private fun setupListeners() {
        binding.adjFAB.setOnClickListener {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    galleryImage()
                } else {
                    reqStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                showError("Error al abrir la galería: ${e.message}")
            }
        }

        binding.IbBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.sendFAB.setOnClickListener {
            validateMessage()
        }
    }

    private fun loadMessages() {
        val messageArrayList = ArrayList<Chat>()
        val ref = FirebaseDatabase.getInstance().getReference("Chats")

        ref.child(chatRoute).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    messageArrayList.clear()
                    snapshot.children.forEach { ds ->
                        ds.getValue(Chat::class.java)?.let { chat ->
                            messageArrayList.add(chat)
                        }
                    }

                    val adapterChat = ChatAdapter(this@ChatActivity, messageArrayList)
                    binding.chatsRV.adapter = adapterChat
                    binding.chatsRV.layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                        stackFromEnd = true
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    showError("Error al cargar los mensajes: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                FirebaseCrashlytics.getInstance().recordException(error.toException())
                showError("Error en la base de datos: ${error.message}")
            }
        })
    }

    private fun validateMessage() {
        val message = binding.etMsgChat.text.toString().trim()
        val time = Const.getTimeD()

        if (message.isEmpty()) {
            Toast.makeText(this, "Ingrese un mensaje", Toast.LENGTH_SHORT).show()
        } else {
            sendMessage(Const.MESSAGE_TYPE_TEXT, message, time)
        }
    }

    private fun loadInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")

        ref.child(uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val name = snapshot.child("names").value.toString()
                    val image = snapshot.child("image").value.toString()
                    val status = snapshot.child("status").value.toString()

                    binding.txtStatus.text = status
                    binding.txtUserName.text = name

                    Glide.with(this@ChatActivity)
                        .load(image)
                        .placeholder(R.drawable.user_profile)
                        .into(binding.toolbarIV)
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                    showError("Error al cargar la información del usuario: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                FirebaseCrashlytics.getInstance().recordException(error.toException())
                showError("Error en la base de datos: ${error.message}")
            }
        })
    }

    private fun galleryImage() {
        try {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            resultGalleryARL.launch(intent)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            showError("Error al abrir la galería: ${e.message}")
        }
    }

    private val resultGalleryARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            try {
                if (res.resultCode == Activity.RESULT_OK) {
                    res.data?.data?.let { uri ->
                        imageUri = uri
                        subStorageImage()
                    } ?: run {
                        showError("Error al seleccionar la imagen.")
                    }
                } else {
                    showError("Selección de imagen cancelada.")
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().recordException(e)
                showError("Error al manejar la imagen seleccionada: ${e.message}")
            }
        }

    private val reqStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                galleryImage()
            } else {
                showError("El permiso de almacenamiento no fue concedido.")
            }
        }

    private fun subStorageImage() {
        if (imageUri == null) {
            showError("No se seleccionó ninguna imagen.")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        val time = Const.getTimeD()
        val routeNameImage = "chatImages/$time"
        val storageRef = FirebaseStorage.getInstance().getReference(routeNameImage)

        storageRef.putFile(imageUri!!).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                sendMessage(Const.MESSAGE_TYPE_IMAGE, uri.toString(), time)
            }.addOnFailureListener { e ->
                FirebaseCrashlytics.getInstance().recordException(e)
                showError("Error al obtener la URL de la imagen: ${e.message}")
            }
        }.addOnFailureListener { e ->
            FirebaseCrashlytics.getInstance().recordException(e)
            showError("Error al subir la imagen: ${e.message}")
        }.addOnCompleteListener {
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun sendMessage(messageTypeText: String, message: String, time: Long) {
        val refChat = FirebaseDatabase.getInstance().getReference("Chats")
        val keyId = refChat.push().key.orEmpty()

        val hashMap = hashMapOf(
            "idMessage" to keyId,
            "typeMessage" to messageTypeText,
            "message" to message,
            "transmitterUid" to miUid,
            "receiverUid" to uid,
            "time" to time
        )

        refChat.child(chatRoute).child(keyId).setValue(hashMap)
            .addOnSuccessListener {
                binding.etMsgChat.setText("")
            }
            .addOnFailureListener { e ->
                FirebaseCrashlytics.getInstance().recordException(e)
                showError("Error al enviar el mensaje: ${e.message}")
            }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        FirebaseCrashlytics.getInstance().log(message)
    }

    private fun userStatus(status: String) {
        val userId = firebaseAuth.uid ?: return
        val ref = FirebaseDatabase.getInstance().getReference("Users").child(userId)

        ref.updateChildren(mapOf("status" to status)).addOnFailureListener { e ->
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun onResume() {
        super.onResume()
        userStatus("online")
    }

    override fun onPause() {
        super.onPause()
        userStatus("offline")
    }
}
