package com.example.chat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.chat.databinding.ActivityEditInformationBinding
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.storage.FirebaseStorage

class EditInformation : AppCompatActivity() {

    private lateinit var binding : ActivityEditInformationBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var crashlytics: FirebaseCrashlytics
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private lateinit var progressDialog: ProgressDialog
    private var imageUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInformationBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        crashlytics = FirebaseCrashlytics.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        loadInformation()

        remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600) // Actualización cada 1 hora
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.setDefaultsAsync(mapOf("show_button_premium" to true))

        fetchAndApplyRemoteConfig()

        binding.IbBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivEditImage.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                openGallery()
            } else {
                reqStoragePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        binding.btnUpdate.setOnClickListener {
            validateInfo()
        }

        binding.btnPremium.setOnClickListener {
            initError()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchAndApplyRemoteConfig() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val showButtonPremium = remoteConfig.getBoolean("show_button_premium")
                    applyButtonVisibility(showButtonPremium)
                }
            }
    }

    private fun applyButtonVisibility(isVisible: Boolean) {
        val premiumButton = findViewById<MaterialButton>(R.id.btn_premium)

        premiumButton.visibility = if (isVisible) View.VISIBLE else View.GONE // Controla la visibilidad del botón en base al valor booleano
    }

    private fun initError() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            crashlytics.setUserId(user.uid)
        } else {
            crashlytics.setUserId("usuario_no_autenticado")
        }

        crashlytics.log("Crash manual activado - Botón 2")
        crashlytics.setCustomKey("user_action", "Second Crash")
        throw IllegalStateException("Crash Manual: Botón 2 - Estado Ilegal")
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryARL.launch(intent)
    }

    private val galleryARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ res ->
            if(res.resultCode == Activity.RESULT_OK){
                val data = res.data
                imageUri = data!!.data
                uploadImageStorage(imageUri)
            } else{
                Toast.makeText(
                    this,
                    "Cancelado",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    private fun uploadImageStorage(imageUri: Uri?) {
        progressDialog.setMessage("Subiendo imagen a Storage")
        progressDialog.show()

        val routeImage = "profileImages/" + firebaseAuth.uid
        val ref = FirebaseStorage.getInstance().getReference(routeImage)
        ref.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while(!uriTask.isSuccessful);
                val urlImageUpload = uriTask.result.toString()
                if(uriTask.isSuccessful){
                    updateInfoDB(urlImageUpload)
                }

            }
            .addOnFailureListener{ e ->

            }
    }

    private fun updateInfoDB(urlImageUpload: String) {
        progressDialog.setMessage("Actualizando imagen")
        progressDialog.show()

        val hashMap : HashMap<String, Any> = HashMap()
        if(imageUri != null){
            hashMap["image"] = urlImageUpload
        }

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Su imagen de perfil se ha actualizado",
                    Toast.LENGTH_SHORT
                ).show()

            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private val reqStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
            if(isGranted){
                openGallery()
            } else {
                Toast.makeText(
                    this,
                    "El permiso de almacenamiento ha sido denegado",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

    private var names = ""

    private fun validateInfo() {
         names = binding.etName.text.toString()

        if (names.isEmpty()){
            binding.etName.error = "Ingrese nombres"
            binding.etName.requestFocus()
        } else {
            updateInformation()
        }
    }

    private fun updateInformation() {
        progressDialog.setMessage("Actualizando informacion")
        progressDialog.show()

        val hashMap : HashMap<String, Any> = HashMap()
        hashMap["names"] = names

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    applicationContext,
                    "Se actualizo su informacion",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    applicationContext,
                    "${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

    }

    private fun loadInformation() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val names = "${snapshot.child("names").value}"
                    val image = "${snapshot.child("image").value}"

                    binding.etName.setText(names)

                    try {
                        Glide.with(applicationContext)
                            .load(image)
                            .placeholder(R.drawable.ic_img_profile)
                            .into(binding.ivProfile)
                    }catch (e : Exception){
                        Toast.makeText(
                            applicationContext,
                            "${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}