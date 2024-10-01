package com.example.chat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chat.databinding.ActivityLoginEmailBinding

class LoginEmailActivity : AppCompatActivity() {


   private lateinit var binding : ActivityLoginEmailBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_email)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding = ActivityLoginEmailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvRecoveryAccount.setOnClickListener{
            startActivity(Intent(applicationContext, RecoverPassword::class.java))
        }

        binding.tvRegister.setOnClickListener{
            Log.d("RegisterClick", "Clicked on Register")
            startActivity(Intent(applicationContext, RegisterEmailActivity::class.java))
        }
    }
}