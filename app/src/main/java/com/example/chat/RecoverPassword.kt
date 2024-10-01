package com.example.chat

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chat.databinding.ActivityRecoverPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class RecoverPassword : AppCompatActivity() {

    private lateinit var binding : ActivityRecoverPasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRecoverPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.IBBack.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnSubmit.setOnClickListener {
            validateInfo()
        }
    }

    private var email = ""

    private fun validateInfo() {
        email = binding.etEmail.text.toString().trim()

        if(email.isEmpty()){
            binding.etEmail.error = "Ingrese su email"
            binding.etEmail.requestFocus()
        } else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.etEmail.error = "Email no valido"
            binding.etEmail.requestFocus()
        } else{
            sendInstructions()
        }
    }

    private fun sendInstructions() {
        progressDialog.setMessage("Enviando las instrucciones a ${email}")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()

                Toast.makeText(
                    this,
                    "Instrucciones enviadas con exito",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener{ e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Fallo el envio de instrucciones debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }
    }
}