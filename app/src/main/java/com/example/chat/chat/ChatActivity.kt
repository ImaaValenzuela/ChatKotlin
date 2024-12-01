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
    private lateinit var progressDialog: ProgressDialog

    private var uid = "" // uid del receptor
    private var miUid = "" // uid del emisor
    private var chatRoute = ""
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this).apply {
            setTitle("Espere por favor")
            setCanceledOnTouchOutside(false)
        }

        uid = intent.getStringExtra("uid") ?: ""
        miUid = firebaseAuth.uid.orEmpty()

        FirebaseCrashlytics.getInstance().setUserId(uid) // Configurar ID de usuario y modelo de dispositivo en Crashlytics
        FirebaseCrashlytics.getInstance().setCustomKey("Device_Model", Build.MODEL)

        chatRoute = Const.routeChat(uid, miUid)

        setupListeners()
        loadInfo()
        loadMessages()
    }

    private fun setupListeners() {
        binding.adjFAB.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                galleryImage()
            } else {
                reqStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
            }

            override fun onCancelled(error: DatabaseError) {
                FirebaseCrashlytics.getInstance().recordException(error.toException())
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
                val name = snapshot.child("names").value.toString()
                val image = snapshot.child("image").value.toString()
                val status = snapshot.child("status").value.toString()

                binding.txtStatus.text = status
                binding.txtUserName.text = name

                Glide.with(this@ChatActivity)
                    .load(image)
                    .placeholder(R.drawable.user_profile)
                    .into(binding.toolbarIV)
            }

            override fun onCancelled(error: DatabaseError) {
                FirebaseCrashlytics.getInstance().recordException(error.toException())
            }
        })
    }

    private fun galleryImage() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        resultGalleryARL.launch(intent)
    }

    private val resultGalleryARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == Activity.RESULT_OK) {
                res.data?.data?.let { uri ->
                    imageUri = uri
                    subStorageImage()
                } ?: run {
                    showError("Ocurrió un error al seleccionar la imagen")
                }
            } else {
                showError("Cancelado")
            }
        }

    private val reqStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                galleryImage()
            } else {
                showError("El permiso de almacenamiento no fue concedido")
            }
        }

    private fun subStorageImage() {
        progressDialog.setMessage("Subiendo Imagen")
        progressDialog.show()

        val time = Const.getTimeD()
        val routeNameImage = "chatImages/$time"
        val storageRef = FirebaseStorage.getInstance().getReference(routeNameImage)
        storageRef.putFile(imageUri!!).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                sendMessage(Const.MESSAGE_TYPE_IMAGE, uri.toString(), time)
            }.addOnFailureListener { e ->
                showError("Error al obtener URL de la imagen: ${e.message}")
            }
        }.addOnFailureListener { e ->
            showError("No se pudo enviar la imagen debido a: ${e.message}")
        }
    }

    private fun sendMessage(messageTypeText: String, message: String, time: Long) {
        progressDialog.setMessage("Enviando Mensaje")
        progressDialog.show()

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
                progressDialog.dismiss()
                binding.etMsgChat.setText("")
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                showError("No se pudo enviar el mensaje debido a: ${e.message}")
            }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        FirebaseCrashlytics.getInstance().log(message)
    }

    private fun userStatus(status : String) {
        val userId = firebaseAuth.uid
        if (userId != null) {
            val ref = FirebaseDatabase.getInstance().getReference("Users").child(userId)
            val hashMap = HashMap<String, Any>()
            hashMap["status"] = status

            // Usamos el método updateChildren para actualizar el estado del usuario
            ref.updateChildren(hashMap).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Actualización exitosa
                    FirebaseCrashlytics.getInstance().log("Status updated to: $status")
                } else {
                    // En caso de error, registrar el error
                    FirebaseCrashlytics.getInstance().recordException(task.exception ?: Exception("Unknown error"))
                }
            }
        } else {
            FirebaseCrashlytics.getInstance().log("User not authenticated.")
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
