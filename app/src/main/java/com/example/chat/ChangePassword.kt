package com.example.chat

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chat.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ChangePassword : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        setupProgressDialog()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.IbBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnChangePass.setOnClickListener {
            validateInfo()
        }
    }

    private fun setupProgressDialog() {
        val progressBar = ProgressBar(this).apply {
            isIndeterminate = true
        }

        progressDialog = AlertDialog.Builder(this)
            .setTitle("Espere por favor")
            .setView(progressBar)
            .setCancelable(false)
            .create()
    }

    private fun validateInfo() {
        val actualPass = binding.etActualPass.text.toString().trim()
        val newPass = binding.etNewPass.text.toString().trim()
        val rNewPass = binding.etRNewPass.text.toString().trim()

        when {
            actualPass.isEmpty() -> {
                binding.etActualPass.error = "Ingrese contraseña actual"
                binding.etActualPass.requestFocus()
            }
            newPass.isEmpty() -> {
                binding.etNewPass.error = "Ingrese nueva contraseña"
                binding.etNewPass.requestFocus()
            }
            rNewPass.isEmpty() -> {
                binding.etRNewPass.error = "Repita nueva contraseña"
                binding.etRNewPass.requestFocus()
            }
            newPass != rNewPass -> {
                binding.etRNewPass.error = "No coinciden las contraseñas"
                binding.etRNewPass.requestFocus()
            }
            else -> authUser(actualPass, newPass)
        }
    }

    private fun authUser(actualPass: String, newPass: String) {
        progressDialog.show()

        val authCredential = EmailAuthProvider.getCredential(firebaseUser.email.toString(), actualPass)
        firebaseUser.reauthenticate(authCredential)
            .addOnSuccessListener { updatePassword(newPass) }
            .addOnFailureListener { e -> handleError(e.message) }
    }

    private fun updatePassword(newPass: String) {
        progressDialog.setMessage("Cambiando contraseña")

        firebaseUser.updatePassword(newPass)
            .addOnSuccessListener {
                progressDialog.dismiss()
                showToast("La contraseña se ha actualizado")
                logout()
            }
            .addOnFailureListener { e -> handleError(e.message) }
    }

    private fun handleError(errorMessage: String?) {
        progressDialog.dismiss()
        showToast("Fallo la autenticacion debido a $errorMessage")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun logout() {
        firebaseAuth.signOut()
        startActivity(Intent(applicationContext, OptionsLoginActivity::class.java))
        finishAffinity()
    }
}
