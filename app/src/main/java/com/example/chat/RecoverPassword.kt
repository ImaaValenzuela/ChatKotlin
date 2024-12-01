package com.example.chat

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chat.databinding.ActivityRecoverPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class RecoverPassword : AppCompatActivity() {

    private lateinit var binding: ActivityRecoverPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRecoverPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aplicar padding dinámico para el sistema de barras
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

        // Acción para regresar a la pantalla anterior
        binding.IBBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Acción del botón para enviar el correo
        binding.btnSubmit.setOnClickListener {
            validateInfo()
        }
    }

    private fun validateInfo() {
        val email = binding.etEmail.text.toString().trim()

        when {
            email.isEmpty() -> {
                binding.etEmail.error = "Ingrese su email"
                binding.etEmail.requestFocus()
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Email no válido"
                binding.etEmail.requestFocus()
            }
            else -> sendInstructions(email)
        }
    }

    private fun sendInstructions(email: String) {
        showLoading(true)

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(
                    this,
                    "Instrucciones enviadas con éxito",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Fallo el envío de instrucciones debido a: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSubmit.isEnabled = !isLoading
    }
}
