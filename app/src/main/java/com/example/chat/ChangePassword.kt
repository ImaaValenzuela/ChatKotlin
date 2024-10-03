package com.example.chat

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
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

    private lateinit var binding : ActivityChangePasswordBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var progressDialog : ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityChangePasswordBinding.inflate(layoutInflater)

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere por favor")
        progressDialog.setCanceledOnTouchOutside(false)


        binding.IbBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnChangePass.setOnClickListener {
            validateInfo()
        }

    }


    private var actual_pass = ""
    private var new_pass = ""
    private var r_new_pass = ""

    private fun validateInfo() {
        actual_pass = binding.etActualPass.text.toString().trim()
        new_pass = binding.etNewPass.text.toString().trim()
        r_new_pass = binding.etRNewPass.toString().trim()

        if(actual_pass.isEmpty()){
            binding.etActualPass.error = "Ingrese contraseña actual"
            binding.etActualPass.requestFocus()
        } else if (new_pass.isEmpty()){
            binding.etNewPass.error = "Ingrese nueva contraseña"
            binding.etNewPass.requestFocus()
        } else if (r_new_pass.isEmpty()) {
            binding.etRNewPass.error = "Repita nueva contraseña"
            binding.etRNewPass.requestFocus()
        } else if(new_pass != r_new_pass){
            binding.etRNewPass.error = "No coinciden las contraseñas"
            binding.etRNewPass.requestFocus()
        } else {
            authUser()
        }
    }

    private fun authUser() {
        progressDialog.setMessage("Autenticando Usuario")
        progressDialog.show()

        val authCredential = EmailAuthProvider.getCredential(firebaseUser.email.toString(), actual_pass)
        firebaseUser.reauthenticate(authCredential)
            .addOnSuccessListener {
                updatePassword()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Fallo la autenticacion debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

            }
    }

    private fun updatePassword() {
        progressDialog.setMessage("Cambiando contraseña")
        progressDialog.show()

        firebaseUser.updatePassword(new_pass)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "La contraseña se ha actualizado",
                    Toast.LENGTH_SHORT
                ).show()
                logout()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    "Fallo la autenticacion debido a ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun logout() {
        firebaseAuth.signOut()
        startActivity(Intent(applicationContext, OptionsLoginActivity::class.java))
        finishAffinity()
    }
}