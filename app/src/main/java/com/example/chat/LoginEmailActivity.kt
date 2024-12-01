package com.example.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chat.databinding.ActivityLoginEmailBinding
import com.google.firebase.auth.FirebaseAuth

class LoginEmailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginEmailBinding

    companion object {
        private const val TAG = "LoginEmailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.tvRecoveryAccount.setOnClickListener { navigateToRecoverPassword() }
        binding.tvRegister.setOnClickListener { navigateToRegister() }
        binding.btnEnter.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Llama al método de autenticación
            loginUser(email, password)
        }

    }

    private fun navigateToRecoverPassword() {
        startActivity(Intent(applicationContext, RecoverPassword::class.java))
    }

    private fun navigateToRegister() {
        try {
            startActivity(Intent(applicationContext, RegisterEmailActivity::class.java))
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to RegisterEmailActivity: ${e.message}")
        }
    }

    private fun loginUser(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("LoginSuccess", "User logged in successfully")
                    // Redirige al usuario a la siguiente actividad
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Log.e("LoginError", "Login failed: ${task.exception?.message}")
                }
            }
    }

}
