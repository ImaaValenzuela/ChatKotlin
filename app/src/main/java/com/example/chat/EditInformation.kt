package com.example.chat

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.chat.databinding.ActivityEditInformationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class EditInformation : AppCompatActivity() {

    private lateinit var binding: ActivityEditInformationBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var crashlytics: FirebaseCrashlytics
    private var imageUri: Uri? = null

    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicialización de Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        crashlytics = FirebaseCrashlytics.getInstance()

        // Configuración del ProgressDialog utilizando AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setView(R.layout.progress_bar)  // Asumiendo que tienes un layout progress_bar.xml con un ProgressBar
        builder.setCancelable(false)
        progressDialog = builder.create()

        // Cargar la información inicial
        loadInformation()

        // Configurar los listeners
        binding.IbBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.ivEditImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

        // Ajustar el padding para la vista principal
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Función para abrir la galería de imágenes
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryARL.launch(intent)
    }

    // Llamada al launcher para seleccionar la imagen
    private val galleryARL =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == Activity.RESULT_OK) {
                val data = res.data
                imageUri = data?.data
                uploadImageStorage(imageUri)
            } else {
                Toast.makeText(this, "Cancelado", Toast.LENGTH_SHORT).show()
            }
        }

    // Subir la imagen seleccionada a Firebase Storage
    private fun uploadImageStorage(imageUri: Uri?) {
        progressDialog.show()

        val routeImage = "profileImages/" + firebaseAuth.uid
        val ref = FirebaseStorage.getInstance().getReference(routeImage)
        ref.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val urlImageUpload = uri.toString()
                    updateInfoDB(urlImageUpload)
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Actualizar la información en la base de datos
    private fun updateInfoDB(urlImageUpload: String) {
        progressDialog.show()

        val hashMap: HashMap<String, Any> = HashMap()
        if (imageUri != null) {
            hashMap["image"] = urlImageUpload
        }

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Imagen de perfil actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Error al actualizar imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Solicitar permisos para acceder al almacenamiento
    private val reqStoragePermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(this, "Permiso de almacenamiento denegado", Toast.LENGTH_SHORT).show()
            }
        }

    // Validar los datos antes de actualizar
    private fun validateInfo() {
        val names = binding.etName.text.toString()

        if (names.isEmpty()) {
            binding.etName.error = "Ingrese nombres"
            binding.etName.requestFocus()
        } else {
            updateInformation(names)
        }
    }

    // Actualizar la información del usuario
    private fun updateInformation(names: String) {
        progressDialog.show()

        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["names"] = names

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "Información actualizada", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(applicationContext, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Cargar la información actual del usuario
    private fun loadInformation() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .get()
            .addOnSuccessListener { snapshot ->
                val names = snapshot.child("names").value.toString()
                val image = snapshot.child("image").value.toString()

                binding.etName.setText(names)

                try {
                    Glide.with(applicationContext)
                        .load(image)
                        .placeholder(R.drawable.ic_img_profile)
                        .into(binding.ivProfile)
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "Error al cargar imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(applicationContext, "Error al cargar información: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Simulación de error para Crashlytics
    private fun initError() {
        val user = firebaseAuth.currentUser
        crashlytics.setUserId(user?.uid ?: "usuario_no_autenticado")
        crashlytics.log("Crash manual activado - Botón 2")
        crashlytics.setCustomKey("user_action", "Second Crash")
        throw IllegalStateException("Crash Manual: Botón 2 - Estado Ilegal")
    }
}
