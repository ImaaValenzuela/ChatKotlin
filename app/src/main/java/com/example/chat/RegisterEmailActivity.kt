package com.example.chat

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chat.databinding.ActivityRegisterEmailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase

class RegisterEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterEmailBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            validateInfo()
        }

        // Manejar insets para un diseño edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private var names = ""
    private var email = ""
    private var password = ""
    private var rPassword = ""

    private fun validateInfo() {
        names = binding.etName.text.toString().trim()
        email = binding.etEmail.text.toString().trim()
        password = binding.etPassword.text.toString().trim()
        rPassword = binding.etRPassword.text.toString().trim()

        when {
            names.isEmpty() -> {
                binding.etName.error = "Ingrese nombre"
                binding.etName.requestFocus()
            }
            email.isEmpty() -> {
                binding.etEmail.error = "Ingrese email"
                binding.etEmail.requestFocus()
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Email inválido"
                binding.etEmail.requestFocus()
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Ingrese contraseña"
                binding.etPassword.requestFocus()
            }
            rPassword.isEmpty() -> {
                binding.etRPassword.error = "Repita contraseña"
                binding.etRPassword.requestFocus()
            }
            password != rPassword -> {
                binding.etRPassword.error = "No coinciden las contraseñas"
                binding.etRPassword.requestFocus()
            }
            else -> registerUser()
        }
    }

    private fun registerUser() {
        showProgress(true)
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                updateInfo()
            }
            .addOnFailureListener { e ->
                showProgress(false)
                val errorMessage = when (e) {
                    is FirebaseAuthUserCollisionException -> "Este correo ya está registrado."
                    is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil."
                    else -> "Error al crear cuenta: ${e.message}"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateInfo() {
        val uidU = firebaseAuth.uid ?: return
        val namesU = names
        val emailU = firebaseAuth.currentUser?.email ?: "No disponible"
        val timeR = Const.getTimeD()

        val dataUser = mapOf(
            "uid" to uidU,
            "names" to namesU,
            "email" to emailU,
            "time" to timeR,
            "prov" to "email",
            "status" to "online",
            "image" to ""
        )

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(uidU)
            .setValue(dataUser)
            .addOnSuccessListener {
                showProgress(false)
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finishAffinity()
            }
            .addOnFailureListener { e ->
                showProgress(false)
                Toast.makeText(this, "Error al guardar información: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}
